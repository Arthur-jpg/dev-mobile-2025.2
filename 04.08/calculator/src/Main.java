import control.Calculadora;

public class Main {
    public static void main(String[] args) {
      Calculadora calculadora = new Calculadora(10, 5);
      calculadora.somar();
      calculadora.dividir();
      calculadora.multiplicar();
      calculadora.subtrair();
    }
}