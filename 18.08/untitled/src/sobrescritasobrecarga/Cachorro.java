package sobrescritasobrecarga;

public class Cachorro extends Animal {

    // Na mesma classe temos sobrescrita e sobrecarga
    @Override
    public void fazerSom() {
        System.out.println("au au au au");
    }

    @Override
    public void comer(String alimento) {
        System.out.println("Comendo " + alimento);
    }

    public void comer(String alimento, int qtd) {
        System.out.println("Cachorro comendo " + qtd + " de " + alimento);
    }
}
