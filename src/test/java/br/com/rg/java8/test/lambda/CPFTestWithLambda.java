package br.com.rg.java8.test.lambda;

import br.com.rg.java8.abstracao.Validador;

public class CPFTestWithLambda {
    public static void main(String[] args) {
        Validador<String> cep = valor -> valor.matches("[0-9]{5}-[0-9]{3}");
        System.out.println(cep.validar("06381-470"));
    }
}
