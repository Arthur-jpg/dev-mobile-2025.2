package control;

public class Calculadora {
    private double primeiroNumero;
    private double segundoNumero;

    public Calculadora(double primeiroNumero, double segundoNumero) {
        this.primeiroNumero = primeiroNumero;
        this.segundoNumero = segundoNumero;
    }

    public double somar() {
        double resultado = primeiroNumero + segundoNumero;
        System.out.println("Soma: " + resultado);
        return resultado;
    }

    public double subtrair() {
        double resultado = primeiroNumero - segundoNumero;
        System.out.println("Subtração: " + resultado);
        return resultado;
    }

    public double multiplicar() {
        double resultado = primeiroNumero * segundoNumero;
        System.out.println("Multiplicação: " + resultado);
        return resultado;
    }

    public double dividir() {
        if (segundoNumero == 0) {
            System.out.println("Divisão por zero.");
        }
        double resultado = primeiroNumero / segundoNumero;
        System.out.println("Divisão: " + resultado);
        return resultado;
    }

}
