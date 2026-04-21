package tcc.project.dual.ec.drbg;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import java.math.BigInteger;

public class Backdoor {

    public byte[][] executarSimulacaoDualEC(int numOutputsFuturos, X9ECParameters ecParams, ECPoint pointP, ECPoint pointQ, BigInteger seedInicial, BigInteger[] estadosInternosReais) {
        BigInteger s_state = seedInicial;
        byte[][] outputStream = new byte[numOutputsFuturos + 2][];

        for (int i = 0; i < numOutputsFuturos + 2; i++) {
            estadosInternosReais[i] = s_state;
            
            byte[] r_full_bytes = pointQ.multiply(s_state).normalize().getAffineXCoord().getEncoded();
            outputStream[i] = truncarSaidaPadraoNIST(r_full_bytes);
            
            ECPoint s_next_point = pointP.multiply(s_state).normalize();
            s_state = s_next_point.getAffineXCoord().toBigInteger();
        }
        return outputStream;
    }

    public BigInteger executarAtaqueForcaBruta(byte[] r0Observed, byte[] r1Observed, X9ECParameters ecParams, ECPoint pointQ, BigInteger e_inv_backdoor) {
        BigInteger r0_val = new BigInteger(1, r0Observed);
        BigInteger s1_recovered = null;

        System.out.println("--- EXECUTANDO CRIPTOANÁLISE (Brute-force 16-bits) ---");
        int totalTentativas = 0;

        outerLoop:
        for (int i = 0; i < 65536; i++) {
            BigInteger xCandidate = BigInteger.valueOf(i).shiftLeft(240).or(r0_val);

            for (String compressionPrefix : new String[]{"02", "03"}) {
                try {
                    totalTentativas++;
                    byte[] pointBytes = reconstruirPontoComprimido(xCandidate, compressionPrefix);
                    ECPoint pointR0 = ecParams.getCurve().decodePoint(pointBytes);

                    BigInteger s1_candidate = pointR0.multiply(e_inv_backdoor).normalize()
                                                     .getAffineXCoord().toBigInteger();

                    byte[] r1_check_full = pointQ.multiply(s1_candidate).normalize()
                                                .getAffineXCoord().getEncoded();
                    byte[] r1_check_truncated = truncarSaidaPadraoNIST(r1_check_full);

                    if (java.util.Arrays.equals(r1_check_truncated, r1Observed)) {
                        s1_recovered = s1_candidate;
                        System.out.println("[!] Estado interno s1 sincronizado com sucesso!");
                        System.out.printf("[!] Esforço computacional: %,d tentativas.%n", totalTentativas);
                        break outerLoop;
                    }
                } catch (Exception ex) {
                    continue;
                }
            }
        }
        return s1_recovered;
    }

    public void demonstrarPrevisibilidade(int numOutputsFuturos, BigInteger s1Recovered, ECPoint pointP, ECPoint pointQ, byte[][] outputsReaisVítima) {
        System.out.println("\n--- PREDICÇÃO DE FLUXO DE DADOS (STREAM PREDICTION) ---");
        System.out.printf("%-10s %-64s %-8s%n", "Output", "Valor Previsto pelo Atacante", "Status");
        System.out.println("-".repeat(100));

        BigInteger s_attacker_state = s1Recovered;
        int acertos = 0;

        for (int i = 0; i < numOutputsFuturos; i++) {
            s_attacker_state = pointP.multiply(s_attacker_state).normalize()
                                     .getAffineXCoord().toBigInteger();

            byte[] r_pred_full = pointQ.multiply(s_attacker_state).normalize()
                                       .getAffineXCoord().getEncoded();
            byte[] r_predicted = truncarSaidaPadraoNIST(r_pred_full);

            byte[] r_victim_real = outputsReaisVítima[i + 2];

            boolean isMatch = java.util.Arrays.equals(r_predicted, r_victim_real);
            if (isMatch) acertos++;

            System.out.printf("R%-9d %s [%s]%n",
                i + 2, Hex.toHexString(r_predicted), isMatch ? "✓ CONFIRMADO" : "✗ ERRO");
        }

        System.out.println("-".repeat(100));
        System.out.printf("Resultado da Predição: %d/%d acertos.%n", acertos, numOutputsFuturos);
    }

    private byte[] truncarSaidaPadraoNIST(byte[] rawBytes) {
        byte[] truncated = new byte[30];
        System.arraycopy(rawBytes, rawBytes.length - 30, truncated, 0, 30);
        return truncated;
    }

    private static byte[] reconstruirPontoComprimido(BigInteger xCoord, String prefix) {
        byte[] rawX = xCoord.toByteArray();
        byte[] result = new byte[33];
        result[0] = Hex.decode(prefix)[0];
        int srcPos = Math.max(0, rawX.length - 32);
        int destPos = Math.max(1, 33 - (rawX.length - srcPos));
        System.arraycopy(rawX, srcPos, result, destPos, Math.min(32, rawX.length - srcPos));
        return result;
    }
}