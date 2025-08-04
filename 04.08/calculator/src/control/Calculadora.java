package control;

public class Calculadora {

    public double somar(double primeiroNumero, double segundoNumero) {
        return primeiroNumero + segundoNumero;
    }

    public double subtrair(double primeiroNumero, double segundoNumero) {
        return primeiroNumero - segundoNumero;
    }

    public double multiplicar(double primeiroNumero, double segundoNumero) {
        return primeiroNumero * segundoNumero;
    }

    public double dividir(double primeiroNumero, double segundoNumero) {
        if (segundoNumero == 0) {
            System.out.println("Divisão por zero.");
        }
        return primeiroNumero / segundoNumero;
    }

}
