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
var status = 0;

function start() {
    cm.sendSimple("Hello, where would you like to go?\r\n#L0#Untamed Hearts Hunting Ground#l\r\n#L1#I have 7 keys. Bring me to smash boxes#l\r\n#L2#Please warp me out.#l");
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.sendOk("Goodbye then");
        cm.dispose();
        return;
    }
    if (mode == 1)
        status++;
    else
        status--;
    if (status == 1) {
        if (selection < 1)
            cm.warp(680000400);
        else if (selection < 2) {
            if (cm.haveItem(4031217, 7))
                cm.gainItem(4031217, -7);
            else
                cm.sendOk("It seems like you don't have 7 Keys. Kill the cakes and candles in the Untamed Heart Hunting Ground to get keys. ");
        } else if (selection > 1) {
            cm.warp(680000500);
            cm.sendOk("Goodbye. I hope you enjoyed the wedding!");
        }
        cm.dispose();
    }
}