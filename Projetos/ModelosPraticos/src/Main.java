import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

public class Main {
public static void main(String[] args) {
        int tamanhoBytes = 12_500_000;

        gerarInseguro("inseguro.bin", tamanhoBytes);
        gerarSeguro("seguro.bin", tamanhoBytes);
    }

    public static void gerarInseguro(String nomeArquivo, int tamanho) {
        Random prng = new Random(); 
        byte[] buffer = new byte[1024];

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(nomeArquivo))) {
            for (int i = 0; i < tamanho; i += buffer.length) {
                prng.nextBytes(buffer);
                bos.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void gerarSeguro(String nomeArquivo, int tamanho) {
        SecureRandom csprng = new SecureRandom();
        byte[] buffer = new byte[1024];

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(nomeArquivo))) {
            for (int i = 0; i < tamanho; i += buffer.length) {
                csprng.nextBytes(buffer);
                bos.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}