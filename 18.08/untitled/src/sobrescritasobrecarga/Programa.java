package sobrescritasobrecarga;

public class Programa {
    public static void main(String[] args) {

        Cachorro cachorro = new Cachorro();

        cachorro.comer("Racao", 2);
        cachorro.comer("Racao");

    }
}
