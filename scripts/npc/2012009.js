/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
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

/* Riza the Assistant
	Orbis Random Eye Change.
*/
var status = 0;
var beauty = 0;
var price = 1000000;
var mface = Array(20000, 20001, 20002, 20003, 20004, 20005, 20006, 20007, 20008, 20012, 20014);
var fface = Array(21000, 21001, 21002, 21003, 21004, 21005, 21006, 21007, 21008, 21012, 21014);
var facenew = Array();

function start() {
    cm.sendSimple("Hi, I pretty much shouldn't be doing this, but with a #b#t5152004##k, I will do it anyways for you. But don't forget, it will be random!\r\n#L1#I would like to buy a #b#t5152004##k for " + price + " mesos, please!#l\r\n\#L2#I already have a Coupon!#l");
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
    } else {
        status++;
        if (status == 1) {
            if (selection == 1) {
                if (cm.getMeso() >= price) {
                    cm.gainMeso(-price);
                    cm.gainItem(5152004, 1);
                    cm.sendOk("Enjoy!");
                } else
                    cm.sendOk("You don't have enough mesos to buy a coupon!");
                cm.dispose();
            } else if (selection == 2) {
                facenew = Array();
                if (cm.getPlayer().getGender() == 0)
                    for (var i = 0; i < mface.length; i++)
                        facenew.push(mface[i] + cm.getPlayer().getFace() % 1000 - (cm.getPlayer().getFace() % 100));
                else
                    for (var i = 0; i < fface.length; i++)
                        facenew.push(fface[i] + cm.getPlayer().getFace() % 1000 - (cm.getPlayer().getFace() % 100));
                cm.sendYesNo("If you use the regular coupon, your face may transform into a random new look...do you still want to do it using #b#t5152004##k?");
            }
        }
        else if (status == 2) {
            cm.dispose();
            if (cm.haveItem(5152004)) {
                cm.gainItem(5152004, -1);
                cm.setFace(facenew[Math.floor(Math.random() * facenew.length)]);
                cm.sendOk("Enjoy your new and improved face!");
            } else
                cm.sendOk("Hmm ... it looks like you don't have the coupon specifically for this place. Sorry to say this, but without the coupon, there's no plastic surgery for you...");
        }
    }
}
