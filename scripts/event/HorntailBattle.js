/**
 * @author: Ronan
 * @event: Horntail Battle
 */

importPackage(Packages.server.life);

var isPq = true;
var minPlayers = 6, maxPlayers = 30;
var minLevel = 100, maxLevel = 255;
var entryMap = 240060000;
var exitMap = 240050600;
var recruitMap = 240050400;
var clearMap = 240050600;

var minMapId = 240060000;
var maxMapId = 240060200;

var eventTime = 15;     // 15 minutes

var lobbyRange = [0, 0];

function init() {
    setEventRequirements();
}

function setLobbyRange() {
    return lobbyRange;
}

function setEventRequirements() {
    var reqStr = "";

    reqStr += "\r\n    Number of players: ";
    if (maxPlayers - minPlayers >= 1) reqStr += minPlayers + " ~ " + maxPlayers;
    else reqStr += minPlayers;

    reqStr += "\r\n    Level range: ";
    if (maxLevel - minLevel >= 1) reqStr += minLevel + " ~ " + maxLevel;
    else reqStr += minLevel;

    reqStr += "\r\n    Time limit: ";
    reqStr += eventTime + " minutes";

    em.setProperty("party", reqStr);
}

function setEventExclusives(eim) {
    var itemSet = [];
    eim.setExclusiveItems(itemSet);
}

function setEventRewards(eim) {
    var itemSet, itemQty, evLevel, expStages, mesoStages;

    evLevel = 1;    //Rewards at clear PQ
    itemSet = [];
    itemQty = [];
    eim.setEventRewards(evLevel, itemSet, itemQty);

    expStages = [];    //bonus exp given on CLEAR stage signal
    eim.setEventClearStageExp(expStages);

    mesoStages = [];    //bonus meso given on CLEAR stage signal
    eim.setEventClearStageMeso(mesoStages);
}

function afterSetup(eim) {
}

function setup(channel) {
    var eim = em.newInstance("Scarga" + channel);
    eim.setProperty("canJoin", 1);
    eim.setProperty("defeatedBoss", 0);
    eim.setProperty("defeatedHead", 0);

    var level = 1;
    eim.getInstanceMap(240060000).resetPQ(level);
    eim.getInstanceMap(240060100).resetPQ(level);
    eim.getInstanceMap(240060200).resetPQ(level);

    var map, mob;
    map = eim.getInstanceMap(240060000);
    mob = MapleLifeFactory.getMonster(8810000);
    map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(960, 120));

    map = eim.getInstanceMap(240060100);
    mob = MapleLifeFactory.getMonster(8810001);
    map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(-420, 120));

    eim.startEventTimer(eventTime * 60000);
    setEventRewards(eim);
    setEventExclusives(eim);

    return eim;
}

function playerEntry(eim, player) {
    eim.dropMessage(5, "[Expedition] " + player.getName() + " has entered the map.");
    var map = eim.getMapInstance(entryMap);
    player.changeMap(map, map.getPortal(0));
}

function scheduledTimeout(eim) {
    end(eim);
}

function changedMap(eim, player, mapid) {
    if (mapid < minMapId || mapid > maxMapId) {
        if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
            eim.dropMessage(5, "[Expedition] Either the leader has quitted the event or there is no longer the minimum number of members required to continue this event.");
            eim.unregisterPlayer(player);
            end(eim);
        }
        else {
            eim.dropMessage(5, "[Expedition] " + player.getName() + " has left the event.");
            eim.unregisterPlayer(player);
        }
    }
}

function changedLeader(eim, leader) {
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) {
    if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
        eim.unregisterPlayer(player);
        eim.dropMessage(5, "[Expedition] Either the leader has quitted the event or there is no longer the minimum number of members required to continue this event.");
        end(eim);
    }
    else {
        eim.dropMessage(5, "[Expedition] " + player.getName() + " has left the event.");
        eim.unregisterPlayer(player);
    }
}

function playerDisconnected(eim, player) {
    if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
        eim.dropMessage(5, "[Expedition] Either the leader has quitted the event or there is no longer the minimum number of members required to continue this event.");
        eim.unregisterPlayer(player);
        end(eim);
    }
    else {
        eim.dropMessage(5, "[Expedition] " + player.getName() + " has left the event.");
        eim.unregisterPlayer(player);
    }
}

function leftParty(eim, player) {
}

function disbandParty(eim) {
}

function monsterValue(eim, mobId) {
    return 1;
}

function playerUnregistered(eim, player) {
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, 0);
}

function end(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
}

function giveRandomEventReward(eim, player) {
    eim.giveEventReward(player);
}

function clearPQ(eim) {
    eim.stopEventTimer();
    eim.setEventCleared();
}

function isHorntailHead(mob) {
    var mobid = mob.getId();
    return (mobid == 8810000 || mobid == 8810001);
}

function isHorntail(mob) {
    var mobid = mob.getId();
    return (mobid == 8810018);
}

function monsterKilled(mob, eim) {
    if (isHorntail(mob)) {
        eim.setIntProperty("defeatedBoss", 1);
        eim.showClearEffect(mob.getMap().getId());
        eim.clearPQ();

        mob.getMap().broadcastHorntailVictory();
    } else if (isHorntailHead(mob)) {
        var killed = eim.getIntProperty("defeatedHead");
        eim.setIntProperty("defeatedHead", killed + 1);
        eim.showClearEffect(mob.getMap().getId());
    }
}

function allMonstersDead(eim) {
}

function cancelSchedule() {
}

function dispose(eim) {
}
