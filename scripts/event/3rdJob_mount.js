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
/**
 * @Author Ronan
 * 3rd Job Event - Kenta's Mount Quest
 **/
importPackage(Packages.tools);

var entryMap = 923010000;
var exitMap = 923010100;

var minMapId = 923010000;
var maxMapId = 923010000;

var eventMaps = [923010000];

var eventTime = 5; //5 minutes

var lobbyRange = [0, 0];

function setLobbyRange() {
    return lobbyRange;
}

function init() {
    em.setProperty("noEntry", "false");
}

function respawnStages(eim) {
    var i;
    for (i = 0; i < eventMaps.length; i++) {
        eim.getInstanceMap(eventMaps[i]).instanceMapRespawn();
    }

    eim.schedule("respawnStages", 10 * 1000);
}

function playerEntry(eim, player) {
    var mapObj = eim.getInstanceMap(entryMap);

    mapObj.resetPQ(1);
    mapObj.instanceMapForceRespawn();
    respawnStages(eim);

    player.changeMap(entryMap, 0);
    em.setProperty("noEntry", "true");

    player.getClient().getSession().write(MaplePacketCreator.getClock(eventTime * 60));
    eim.startEventTimer(eventTime * 60000);
}

function playerUnregistered(eim, player) {
}

function playerExit(eim, player) {
    var api = player.getClient().getAbstractPlayerInteraction();
    api.removeAll(4031507);
    api.removeAll(4031508);

    eim.unregisterPlayer(player);
    eim.dispose();
    em.setProperty("noEntry", "false");
}

function scheduledTimeout(eim) {
    var player = eim.getPlayers().get(0);
    playerExit(eim, player);
    player.changeMap(exitMap);
}

function playerDisconnected(eim, player) {
    playerExit(eim, player);
}

function changedMap(eim, chr, mapid) {
    if (mapid < minMapId || mapid > maxMapId) playerExit(eim, chr);
}

function clearPQ(eim) {
    eim.stopEventTimer();
    eim.setEventCleared();

    var player = eim.getPlayers().get(0);
    eim.unregisterPlayer(player);
    player.changeMap(exitMap);

    eim.dispose();
    em.setProperty("noEntry", "false");
}

function monsterKilled(mob, eim) {
}

function monsterValue(eim, mobId) {
    return 1;
}

function friendlyKilled(mob, eim) {
    if (em.getProperty("noEntry") != "false") {
        var player = eim.getPlayers().get(0);
        playerExit(eim, player);
        player.changeMap(exitMap);
    }
}

function allMonstersDead(eim) {
}

function cancelSchedule() {
}

function dispose() {
}
