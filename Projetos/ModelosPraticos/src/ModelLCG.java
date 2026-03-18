//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class ModelLCG {
    public static void main(String[] args) {
        long seed = 12345;
        long a = 1664525;
        long c = 1013904223;
        long mod = (long) Math.pow(2, 32);

        LCG lcg = new LCG(seed, a, c, mod);

        for (int i = 0; i < 10; i++) {
            System.out.println(lcg.next());
        }

    }

    public static class LCG {

        private long a;
        private long c;
        private long mod;
        private long xi;

        public LCG(long seed, long a, long c, long mod) {
            this.xi = seed;
            this.a = a;
            this.c = c;
            this.mod = mod;
        }

        public long next() {
            xi = (a * xi + c) % mod;
            return xi;
        }
    }
}