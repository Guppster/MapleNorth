package client;

import server.MapleStatEffect;

public class MapleBuffStatValueHolder
{
    private MapleStatEffect effect;
    private long startTime;
    private int value;
    private boolean bestApplied;

    public MapleBuffStatValueHolder(MapleStatEffect effect, long startTime, int value)
    {
        super();
        this.setEffect(effect);
        this.setStartTime(startTime);
        this.setValue(value);
        this.setBestApplied(false);
    }

    public MapleStatEffect getEffect()
    {
        return effect;
    }

    public void setEffect(MapleStatEffect effect)
    {
        this.effect = effect;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    }

    public int getValue()
    {
        return value;
    }

    public void setValue(int value)
    {
        this.value = value;
    }

    public boolean isBestApplied()
    {
        return bestApplied;
    }

    public void setBestApplied(boolean bestApplied)
    {
        this.bestApplied = bestApplied;
    }
}
