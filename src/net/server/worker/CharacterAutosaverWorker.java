/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.server.worker;

import net.server.PlayerStorage;
import net.server.worker.BaseWorker;
import net.server.world.World;
import client.MapleCharacter;
import constants.ServerConstants;

public class CharacterAutosaverWorker extends BaseWorker implements Runnable
{
    private World wserv;

    public CharacterAutosaverWorker(World world)
    {
        super(world);
    }

    @Override
    public void run()
    {
        if (!ServerConstants.USE_AUTOSAVE) return;

        PlayerStorage ps = wserv.getPlayerStorage();
        for (MapleCharacter chr : ps.getAllCharacters())
        {
            if (chr != null && chr.isLoggedin())
            {
                chr.saveToDB(false);
            }
        }
    }
}
