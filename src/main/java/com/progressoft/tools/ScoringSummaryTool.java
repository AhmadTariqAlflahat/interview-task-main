package com.progressoft.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

public class ScoringSummaryTool implements ScoringSummary {
    private List<BigDecimal> data;
    public List<BigDecimal> getData() {return data;}
    public void setData(List<BigDecimal> data) {this.data = data;}

    public ScoringSummaryTool(List<BigDecimal> returnedCol) {
        setData(returnedCol);
    }

    @Override
    public BigDecimal mean() {
        BigDecimal calcMean;
        BigDecimal sum = getData().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        calcMean = sum.divide(BigDecimal.valueOf(getData().size()), RoundingMode.HALF_EVEN);
        return calcMean.setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public BigDecimal standardDeviation() {
        return BigDecimal.valueOf(Math.sqrt((this.variance().intValue()))).setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public BigDecimal variance() {
        BigDecimal variance;
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal x : getData()) {
            sum = sum.add((x.subtract(this.mean()).pow(2)));
        }
        variance = sum.divide((BigDecimal.valueOf(getData().size())), 0, RoundingMode.HALF_EVEN);
        return variance.setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public BigDecimal median() {
        int size = getData().size();
        getData().sort(Comparator.naturalOrder());
        if (size % 2 == 1) {
            return getData().get((size + 1) / 2 - 1).setScale(2, RoundingMode.HALF_EVEN);
        } else {
            BigDecimal result = getData().get((size) / 2 - 1);
            result = result.add(getData().get((size) / 2));
            return result.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_EVEN);
        }
    }

    @Override
    public BigDecimal min() {
        return getData().stream().reduce(new BigDecimal(1000000), BigDecimal::min).setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public BigDecimal max() {
        return getData().stream().reduce(BigDecimal.ZERO, BigDecimal::max).setScale(2, RoundingMode.HALF_EVEN);
    }


}
