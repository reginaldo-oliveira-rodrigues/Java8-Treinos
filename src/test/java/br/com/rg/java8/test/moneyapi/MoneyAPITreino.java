package br.com.rg.java8.test.moneyapi;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.MonetaryException;
import javax.money.MonetaryOperator;
import javax.money.MonetaryQuery;
import javax.money.NumberValue;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.MonetaryConversions;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;

import org.javamoney.moneta.FastMoney;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.RoundedMoney;
import org.javamoney.moneta.convert.ExchangeRateType;
import org.javamoney.moneta.function.GroupMonetarySummaryStatistics;
import org.javamoney.moneta.function.MonetaryFunctions;
import org.javamoney.moneta.function.MonetaryOperators;
import org.javamoney.moneta.function.MonetarySummaryStatistics;
import org.javamoney.moneta.spi.DefaultNumberValue;

public class MoneyAPITreino {
	public static void main(String[] args) {
		CurrencyUnit real = Monetary.getCurrency("BRL");
		CurrencyUnit dollar = Monetary.getCurrency(Locale.US);
		CurrencyUnit yen = Monetary.getCurrency("JPY");

		// lendo os dados da moeda brasileira real
		System.out.println("Código da Moeda: " + real.getCurrencyCode() + "\nCódigo da Moeda: "
				+ real.getDefaultFractionDigits() + "\nCódigo da Moeda: " + real.getNumericCode());

		// agora que temos exemplos de configuração da moeda, vamos ao seu valor
		// numérico
		NumberValue numero = DefaultNumberValue.of(10.12);

		// isso lança ArithmeticException pois perde precisão
		try{
			numero.longValueExact();
		} catch (ArithmeticException erro){
			erro.printStackTrace();
		}

		// especificando qual classe numérica corresponde exatamente ao valor em
		// dinheiro que queremos
		BigDecimal numBigDecimal = numero.numberValue(BigDecimal.class);

		// mas para não perder precisão
		BigDecimal numBigDecimalExato = numero.numberValueExact(BigDecimal.class);

		// mas para representar o dinheiro mesmo, montante monetário
		// dentro de Money.of, o valor será tratado como BigDecimal
		MonetaryAmount montanteEmBigDecimal = Money.of(10, real);

		// mas com FastMoney.of, o valor será tratado como Long
		MonetaryAmount montanteEmLong = FastMoney.of(10, real);

		// Objetos MonetaryAmount são imutáveis. Somatório de montante1 com
		// montante2
		// dá seu resultado, mas montante1 permanece com seu valor anterior ao
		// somatório.
		MonetaryAmount montante1 = Money.of(10, real);
		MonetaryAmount montante2 = Money.of(10, real);
		System.out.println("Somatório: " + montante1.add(montante2));

		// RoundedMoney armazena valores como BigDecimal e como Moeda,
		// mas realiza operações de arredondamento.
		MonetaryAmount montante3 = RoundedMoney.of(10, real);
		// MonetaryOperator é uma interface funcional com método que recebe um
		// MonetaryAmount e retorna outro
		MonetaryOperator dobro = m -> m.multiply(2);
		System.out.println("Multiplicação de " + montante3 + " por 2: " + montante3.with(dobro));

		// classe utilitária MonetaryOperators com operações predefinidas
		MonetaryAmount montante4 = RoundedMoney.of(10, real, MonetaryOperators.rounding());
		System.out.println("Somatório exagerado: " + montante1.add(Money.of(10.451284654, real)));

		// para extrair valor em formato específico, MonetaryQuery
		MonetaryQuery<BigDecimal> extraiBigDecimal = m -> m.getNumber().numberValue(BigDecimal.class);
		BigDecimal valorExtraido = montante4.query(extraiBigDecimal);
		System.out.println("Valor Extraído: " + valorExtraido);

		// pode-se implementar MonetaryQuery com uma operação, conforme abaixo
		MonetaryQuery<MonetaryAmount> extraiMontante = m -> m.multiply(2);
		// mas, por padrão, MonetaryQuery foi feito só para extração de valor.
		// assim, a forma mais padronizada de usar é para obter o valor
		// em um formato especificado: getNumber().numberValue(BigDecimal.class)

		// apresentando valor em um formato reconhecido pelo usuário final
		MonetaryAmountFormat formato = MonetaryFormats.getAmountFormat(Locale.getDefault());
		System.out.println("Formato padrão da Money API: " + formato.format(montante4));
		
		formato = MonetaryFormats.getAmountFormat(new Locale("PT_BR"));
		System.out.println("Formato em reais: " + formato.format(montante4));
		
		// essa forma abaixo está depreciada
		//MonetaryAmountFormat formato = MonetaryAmountFormatSymbols.getDefafult();
		
		List<MonetaryAmount> dinheiros = Arrays.asList(Money.of(10, real), Money.of(40, real), Money.of(30, real),
				Money.of(15, yen));
		try{
			// usando a corrente de elementos de uma coleção (recurso do Java 8)
			// para fazer uma redução no somatório da lista de valores monetários montada acima.
			// no lugar do somatório ( MonetaryFunctions.sum() ) poderia
			// se obter MonetaryFunctions.min() ou MonetaryFunctions.max()
			System.out.println(dinheiros.stream().reduce(MonetaryFunctions.sum()).get());
		} catch (MonetaryException erro){
			erro.printStackTrace();
		}

		// sumarização do dinheiro
		MonetarySummaryStatistics sumario = dinheiros.stream().collect(MonetaryFunctions.summarizingMonetary(real));
		System.out.println(sumario.getCount());
		System.out.println(sumario.getMin());
		System.out.println(sumario.getMax()); // etc ...

		// filtrando moedas
		System.out.println(dinheiros.stream().filter(MonetaryFunctions.isCurrency(yen)).collect(Collectors.toList()));

		// ordenando moedas
		System.out.println(
				dinheiros.stream().sorted(MonetaryFunctions.sortCurrencyUnitDesc()).collect(Collectors.toList()));

		// ordenando pela moeda mais valiosa
		System.out.println(
				dinheiros.stream().sorted(MonetaryFunctions.sortValuable(MonetaryConversions.getExchangeRateProvider()))
						.collect(Collectors.toList()));

		// comparando se há a moeda que procuramos, o yen
		System.out.println(dinheiros.stream().anyMatch(MonetaryFunctions.isCurrency(yen)));

		// sumário agrupado
		GroupMonetarySummaryStatistics sumarioAgrupado = dinheiros.stream()
				.collect(MonetaryFunctions.groupBySummarizingMonetary());
		System.out.println(sumarioAgrupado.get().get(real));
		// pode-se obter somatório, maior ou menor valor, etc.

		MonetaryAmount reais = Money.of(10, real);
		MonetaryAmount dolares = Money.of(10, dollar);
		try{
			// isso lança MonetaryException pois não se soma moedas diferentes
			MonetaryAmount somatorio = reais.add(dolares);
		} catch (MonetaryException erro){
			erro.printStackTrace();
		}
		
		// usando um provedor de informações de cotação
		ExchangeRateProvider provedor = MonetaryConversions.getExchangeRateProvider(ExchangeRateType.IMF);
		// esse IMF tem que ser do pacote org.javamoney.moneta.convert
		// essa implementação de provedor de cotação busca informação na internet. 
		// até abril de 2016 havia uma discussão sobre se essa API seria incorporada no Java 8 ou se ficaria 
		// como biblioteca externa. Escolheram deixar como biblioteca externa e sugerir aos desenvolvedores que 
		// se não quiserem usar esse provedor que busca cotação na internet que implementem seu próprio provedor
		// buscando informações no banco de dados de sua aplicação.
		
		CurrencyConversion conversorDeMoeda = provedor.getCurrencyConversion(real);
		System.out.println(
				"Somatório de dólar com real formatado para real: "
				+ formato.format(conversorDeMoeda.apply(dolares).add(reais).with(MonetaryOperators.rounding())));
		
		
	}
}
