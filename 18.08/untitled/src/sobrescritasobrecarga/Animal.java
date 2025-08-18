package sobrescritasobrecarga;

abstract class Animal {

    public void fazerSom(){
        System.out.println("Fazendo Som");
    }

    public void comer(String alimento) {
        System.out.println("Comendo " + alimento);
    }

}
