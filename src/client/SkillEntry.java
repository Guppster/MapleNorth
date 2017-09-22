package client;

public class SkillEntry
{
    private int masterlevel;
    private byte skillevel;
    private long expiration;

    public SkillEntry(byte skillevel, int masterlevel, long expiration)
    {
        this.setSkillevel(skillevel);
        this.setExpiration(expiration);
        this.setMasterlevel(masterlevel);
    }

    public int getMasterlevel()
    {
        return masterlevel;
    }

    public void setMasterlevel(int masterlevel)
    {
        this.masterlevel = masterlevel;
    }

    public byte getSkillevel()
    {
        return skillevel;
    }

    public void setSkillevel(byte skillevel)
    {
        this.skillevel = skillevel;
    }

    public long getExpiration()
    {
        return expiration;
    }

    public void setExpiration(long expiration)
    {
        this.expiration = expiration;
    }
}
