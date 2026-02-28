package com.avants.autonomoustrader.dto;

import java.util.List;

public class KiteDto {

    public record HoldingDto(
            String tradingSymbol,
            String exchange,
            String product,
            int quantity,
            int t1Quantity,
            double averagePrice,
            double lastPrice,
            double pnl
    ) {}

    public record PositionDto(
            String tradingSymbol,
            String exchange,
            String product,
            int netQuantity,
            double averagePrice,
            double lastPrice,
            double closePrice,
            double pnl,
            double unrealised,
            double realised,
            double m2m
    ) {}

    public record LivePortfolio(
            List<HoldingDto> holdings,
            List<PositionDto> positions
    ) {}
}
