package tcc.project.dual.ec.drbg;

import org.bouncycastle.asn1.nist.NISTNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import java.math.BigInteger;
import java.security.SecureRandom;

public class Main {
    public static void main(String[] args) {

        Backdoor backdoor = new Backdoor();

        X9ECParameters curve = NISTNamedCurves.getByName("P-256");
        ECPoint P = curve.getG();
        BigInteger n = curve.getN();

        BigInteger d = new BigInteger(256, new SecureRandom()).mod(n);
        ECPoint Q = P.multiply(d).normalize();
        BigInteger e = d.modInverse(n);

        System.out.println("=== SIMULACAO DUAL_EC_DRBG — PREVISÃO DE GERACAO ===\n");

        final int OUTPUTS_FUTUROS = 10;
        BigInteger s = new BigInteger(256, new SecureRandom()).mod(n);

        BigInteger[] estadosReais = new BigInteger[OUTPUTS_FUTUROS + 2];

        byte[][] outputResultante = backdoor.executarSimulacaoDualEC(OUTPUTS_FUTUROS, curve, P, Q, s, estadosReais);

        byte[] r0Capturado = outputResultante[0];
        byte[] r1Capturado = outputResultante[1];

        System.out.println("--- OUTPUTS OBSERVADOS PELO ATACANTE ---");
        System.out.println("R0 (capturado): " + Hex.toHexString(r0Capturado));
        System.out.println("R1 (capturado): " + Hex.toHexString(r1Capturado));
        System.out.println();
        
        BigInteger s1Recuperado = backdoor.executarAtaqueForcaBruta(r0Capturado, r1Capturado, curve, Q, e);
        
        backdoor.demonstrarPrevisibilidade(OUTPUTS_FUTUROS, s1Recuperado, P, Q, outputResultante);
    }
}
