package com.codefathers.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TaxCalculatorUtil {

    public static BigDecimal calculateIR(BigDecimal grossIncome) {
        BigDecimal zero = BigDecimal.ZERO.setScale(2);
        BigDecimal aliquota;
        BigDecimal deduction;

        if (grossIncome.compareTo(new BigDecimal("2428.80")) <= 0) {
            return zero;
        } else if (grossIncome.compareTo(new BigDecimal("2826.65")) <= 0) {
            aliquota = new BigDecimal("0.075");
            deduction = new BigDecimal("182.16");
        } else if (grossIncome.compareTo(new BigDecimal("3751.05")) <= 0) {
            aliquota = new BigDecimal("0.15");
            deduction = new BigDecimal("394.16");
        } else if (grossIncome.compareTo(new BigDecimal("4664.68")) <= 0) {
            aliquota = new BigDecimal("0.225");
            deduction = new BigDecimal("675.49");
        } else {
            aliquota = new BigDecimal("0.275");
            deduction = new BigDecimal("896.00");
        }

        BigDecimal imposto = grossIncome.multiply(aliquota).subtract(deduction);
        System.out.printf("%s %s %s", grossIncome, aliquota, deduction);
        if (imposto.compareTo(zero) < 0) {
            return zero;
        }
        return imposto.setScale(2, RoundingMode.HALF_UP);
    }
}
