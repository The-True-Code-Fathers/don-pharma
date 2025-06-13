package com.codefathers.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TaxCalculatorUtil {

    public static BigDecimal calculateIR(BigDecimal grossIncome) {
        BigDecimal zero = BigDecimal.ZERO.setScale(2);
        BigDecimal aliquota;
        BigDecimal reduce;

        if (grossIncome.compareTo(new BigDecimal("2259.20")) <= 0) {
            return zero;
        } else if (grossIncome.compareTo(new BigDecimal("2826.65")) <= 0) {
            aliquota = new BigDecimal("0.075");
            reduce = new BigDecimal("169.44");
        } else if (grossIncome.compareTo(new BigDecimal("3751.05")) <= 0) {
            aliquota = new BigDecimal("0.15");
            reduce = new BigDecimal("381.44");
        } else if (grossIncome.compareTo(new BigDecimal("4664.68")) <= 0) {
            aliquota = new BigDecimal("0.225");
            reduce = new BigDecimal("662.77");
        } else {
            aliquota = new BigDecimal("0.275");
            reduce = new BigDecimal("896.00");
        }

        BigDecimal imposto = grossIncome.multiply(aliquota).subtract(reduce);

        if (imposto.compareTo(zero) < 0) {
            return zero;
        }
        return imposto.setScale(2, RoundingMode.HALF_UP);
    }
}
