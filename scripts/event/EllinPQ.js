/**
 * @author: Ronan
 * @event: Ellin PQ
 */

var isPq = true;
var minPlayers = 4, maxPlayers = 6;
var minLevel = 44, maxLevel = 55;
var entryMap = 930000000;
var exitMap = 930000800;
var recruitMap = 300030100;
var clearMap = 930000800;

var minMapId = 930000000;
var maxMapId = 930000800;

var eventTime = 30;     // 30 minutes

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

    reqStr += "\r\n    For #radventurers only#k.";

    em.setProperty("party", reqStr);
}

function setEventExclusives(eim) {
    var itemSet = [4001162, 4001163, 4001169, 2270004];
    eim.setExclusiveItems(itemSet);
}

function setEventRewards(eim) {
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
    var eligible = [];
    var hasLeader = false;

    if (party.size() > 0) {
        var partyList = party.toArray();

        for (var i = 0; i < party.size(); i++) {
            var ch = partyList[i];

            if (ch.getMapId() == recruitMap && ch.getLevel() >= minLevel && ch.getLevel() <= maxLevel && Math.floor(ch.getJob().getId() / 1000) == 0) {  //only adventurers
                if (ch.isLeader()) hasLeader = true;
                eligible.push(ch);
            }
        }
    }

    if (!(hasLeader && eligible.length >= minPlayers && eligible.length <= maxPlayers)) eligible = [];
    return eligible;
}

function setup(level, lobbyid) {
    var eim = em.newInstance("Ellin" + lobbyid);
    eim.setProperty("level", level);

    eim.getInstanceMap(930000000).resetPQ(level);
    eim.getInstanceMap(930000100).resetPQ(level);
    eim.getInstanceMap(930000200).resetPQ(level);
    eim.getInstanceMap(930000300).resetPQ(level);
    eim.getInstanceMap(930000400).resetPQ(level);
    var map = eim.getInstanceMap(930000500);
    map.resetPQ(level);
    map.shuffleReactors();
    eim.getInstanceMap(930000600).resetPQ(level);
    eim.getInstanceMap(930000700).resetPQ(level);

    respawnStg2(eim);

    eim.startEventTimer(eventTime * 60000);
    setEventRewards(eim);
    setEventExclusives(eim);
    return eim;
}

function afterSetup(eim) {
}

function respawnStg2(eim) {
    if (!eim.getMapInstance(930000200).getPlayers().isEmpty()) eim.getMapInstance(930000200).instanceMapRespawn();
    eim.schedule("respawnStg2", 4 * 1000);
}

function changedMap(eim, player, mapid) {
    if (mapid < minMapId || mapid > maxMapId) {
        if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
            eim.unregisterPlayer(player);
            end(eim);
        }
        else
            eim.unregisterPlayer(player);
    }
}

function changedLeader(eim, leader) {
    var mapid = leader.getMapId();
    if (!eim.isEventCleared() && (mapid < minMapId || mapid > maxMapId)) {
        end(eim);
    }
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(entryMap);
    player.changeMap(map, map.getPortal(0));
}

function scheduledTimeout(eim) {
    end(eim);
}

function playerUnregistered(eim, player) {
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, 0);
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
        eim.unregisterPlayer(player);
        end(eim);
    }
    else
        eim.unregisterPlayer(player);
}


function playerDisconnected(eim, player) {
    if (eim.isEventTeamLackingNow(true, minPlayers, player))
        end(eim);
    else
        playerExit(eim, player);
}

function leftParty(eim, player) {
    if (eim.isEventTeamLackingNow(false, minPlayers, player))
        end(eim);
    else
        playerExit(eim, player);
}

function disbandParty(eim) {
    end(eim);
}

function monsterValue(eim, mobId) {
    return 1;
}

function end(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
}

function clearPQ(eim) {
    eim.stopEventTimer();
    eim.setEventCleared();

    eim.warpEventTeam(930000800);
}

function monsterKilled(mob, eim) {
}

function allMonstersDead(eim) {
}

function cancelSchedule() {
}

function dispose(eim) {
}
