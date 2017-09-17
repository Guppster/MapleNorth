/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc>
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package server.life;

/**
 * @author LightPepsi
 */

public class MonsterDropEntry
{
    public short questid;

    public int itemId;
    public int chance;
    public int Minimum;
    public int Maximum;

    public MonsterDropEntry(int itemId, int chance, int Minimum, int Maximum, short questid)
    {
        this.itemId = itemId;
        this.chance = chance;
        this.questid = questid;
        this.Minimum = Minimum;
        this.Maximum = Maximum;
    }

    public short getQuestid()
    {
        return questid;
    }

    public void setQuestid(short questid)
    {
        this.questid = questid;
    }

    public int getItemId()
    {
        return itemId;
    }

    public void setItemId(int itemId)
    {
        this.itemId = itemId;
    }

    public int getChance()
    {
        return chance;
    }

    public void setChance(int chance)
    {
        this.chance = chance;
    }

    public int getMinimum()
    {
        return Minimum;
    }

    public void setMinimum(int minimum)
    {
        Minimum = minimum;
    }

    public int getMaximum()
    {
        return Maximum;
    }

    public void setMaximum(int maximum)
    {
        Maximum = maximum;
    }

}