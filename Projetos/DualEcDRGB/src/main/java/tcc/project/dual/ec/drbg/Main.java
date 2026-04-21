import org.bouncycastle.crypto.prng.drbg.DualECSP800DRBG;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class TesteDualEC {
    public static void main(String[] args) {
        // 1. Adicionar o Provider do Bouncy Castle
        Security.addProvider(new BouncyCastleProvider());

        try {
            /* * 2. Instanciar o SecureRandom.
             * O nome do algoritmo no Bouncy Castle para Dual_EC segue este padrão:
             * "DualEC" + [Curva] + "DRBG"
             */
            SecureRandom drbg = SecureRandom.getInstance("DualECP256DRBG", "BC");

            // 3. Gerar bytes aleatórios
            byte[] randomBytes = new byte[32];
            drbg.nextBytes(randomBytes);

            // 4. Exibir o resultado
            System.out.println("--- Teste Dual_EC_DRBG ---");
            System.out.println("Bytes gerados (Base64): " + Base64.getEncoder().encodeToString(randomBytes));

        } catch (Exception e) {
            System.err.println("Erro ao inicializar o algoritmo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}