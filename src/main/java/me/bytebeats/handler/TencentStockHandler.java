package me.bytebeats.handler;

import me.bytebeats.HttpClientPool;
import me.bytebeats.LogUtil;
import me.bytebeats.meta.Stock;

import javax.swing.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TencentStockHandler extends AbsStockHandler {
    private static final long REFRESH_INTERVAL = 3L * 1000L;

    public TencentStockHandler(JTable table, JLabel label) {
        super(table, label);
    }

    @Override
    public String[] getColumnNames() {
        return handleColumnNames(stockColumnNames);
    }

    @Override
    public void load(List<String> symbols) {
        stocks.clear();
        if (timer == null) {
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fetch(symbols);
            }
        }, 0, REFRESH_INTERVAL);
        LogUtil.info("starts updating " + jTable.getToolTipText() + " data");
    }

    private void fetch(List<String> symbols) {
        if (symbols.isEmpty()) {
            return;
        }
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < symbols.size(); i++) {
            if (params.length() != 0) {
                params.append(',');
            }
            params.append(symbols.get(i));
        }
        try {
            String entity = HttpClientPool.getInstance().get(appendParams(params.toString()));
            parse(symbols, entity);
            updateView();
        } catch (Exception e) {
            LogUtil.info(e.getMessage());
            timer.cancel();
            timer = null;
            LogUtil.info("stops updating " + jTable.getToolTipText() + " data because of " + e.getMessage());
        }
    }

    private void parse(List<String> symbols, String entity) {
        String[] raws = entity.split("\n");
        if (symbols.size() != raws.length) {
            return;
        }
        for (int i = 0; i < symbols.size(); i++) {
            String symbol = symbols.get(i);
            String raw = raws[i];
            String assertion = String.format("(?<=v_%s=\").*?(?=\";)", symbol);
            Pattern pattern = Pattern.compile(assertion);
            Matcher matcher = pattern.matcher(raw);
            while (matcher.find()) {
                String[] metas = matcher.group().split("~");
                Stock stock = new Stock();
//                stock.setSymbol(symbol);
//                stock.setName(metas[1]);
//                stock.setLatestPrice(Double.parseDouble(metas[3]));
//                stock.setChange(Double.parseDouble(metas[31]));
//                stock.setChangeRatio(Double.parseDouble(metas[32]));
//                stock.setVolume(Double.parseDouble(metas[36]));
//                stock.setTurnover(Double.parseDouble(metas[37]));
//                stock.setMarketValue(Double.parseDouble(metas[45]));
                //简要信息
                stock.setSymbol(symbol);
                stock.setName(metas[1]);
                stock.setLatestPrice(Double.parseDouble(metas[3]));
                stock.setChange(Double.parseDouble(metas[4]));
                stock.setChangeRatio(Double.parseDouble(metas[5]));
                updateStock(stock);
            }
        }
    }
}
