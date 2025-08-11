package model;

//public class Moto extends Veiculo{
public class Moto implements IVeiculo{
    public String acelerar() {
        return "Acelerando a moto";
    }

    public String frear() {
        return "Freando a moto";
    }
}
