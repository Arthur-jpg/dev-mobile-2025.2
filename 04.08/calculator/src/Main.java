import control.Calculadora;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        Calculadora calculadora = new Calculadora();

        System.out.println("Informe a operação desejada: +, -, *, /");
        char operacao = teclado.next().charAt(0);
        System.out.println("Informe o primeiro número:");
        double primeiroNumero = teclado.nextDouble();
        System.out.println("Informe o segundo número:");
        double segundoNumero = teclado.nextDouble();
        switch (operacao) {
            case '+':
                System.out.println(calculadora.somar(primeiroNumero, segundoNumero));
                break;
            case '-':
                System.out.println(calculadora.subtrair(primeiroNumero, segundoNumero));
                break;
            case '*':
                System.out.println(calculadora.multiplicar(primeiroNumero, segundoNumero));
                break;
            case '/':
                System.out.println(calculadora.dividir(primeiroNumero, segundoNumero));
                break;
            default:
                System.out.println("Operação inválida.");
        }

    }
}