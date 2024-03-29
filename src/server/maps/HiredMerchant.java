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
package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventoryType;
import com.mysql.jdbc.Statement;
import constants.ItemConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.server.Server;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePlayerShopItem;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.Pair;

/**
 */
public class HiredMerchant extends AbstractMapleMapObject
{
    private final List<MaplePlayerShopItem> items = new LinkedList<>();
    private int ownerId, itemId, mesos = 0;
    private int channel, world;
    private long start;
    private String ownerName = "";
    private String description = "";
    private MapleCharacter[] visitors = new MapleCharacter[3];
    private List<Pair<String, Byte>> messages = new LinkedList<>();
    private List<SoldItem> sold = new LinkedList<>();
    private boolean open;
    private MapleMap map;

    public HiredMerchant(final MapleCharacter owner, int itemId, String desc)
    {
        this.setPosition(owner.getPosition());
        this.start = System.currentTimeMillis();
        this.ownerId = owner.getId();
        this.channel = owner.getClient().getChannel();
        this.world = owner.getWorld();
        this.itemId = itemId;
        this.ownerName = owner.getName();
        this.description = desc;
        this.map = owner.getMap();
    }

    private static boolean check(MapleCharacter chr, List<MaplePlayerShopItem> items)
    {
        byte eq = 0, use = 0, setup = 0, etc = 0, cash = 0;
        List<MapleInventoryType> li = new LinkedList<>();
        for (MaplePlayerShopItem item : items)
        {
            final MapleInventoryType invtype = MapleItemInformationProvider.getInstance().getInventoryType(item.getItem().getItemId());
            if (!li.contains(invtype))
            {
                li.add(invtype);
            }
            if (invtype == MapleInventoryType.EQUIP)
            {
                eq++;
            }
            else if (invtype == MapleInventoryType.USE)
            {
                use++;
            }
            else if (invtype == MapleInventoryType.SETUP)
            {
                setup++;
            }
            else if (invtype == MapleInventoryType.ETC)
            {
                etc++;
            }
            else if (invtype == MapleInventoryType.CASH)
            {
                cash++;
            }
        }
        for (MapleInventoryType mit : li)
        {
            if (mit == MapleInventoryType.EQUIP)
            {
                if (chr.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() <= eq)
                {
                    return false;
                }
            }
            else if (mit == MapleInventoryType.USE)
            {
                if (chr.getInventory(MapleInventoryType.USE).getNumFreeSlot() <= use)
                {
                    return false;
                }
            }
            else if (mit == MapleInventoryType.SETUP)
            {
                if (chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() <= setup)
                {
                    return false;
                }
            }
            else if (mit == MapleInventoryType.ETC)
            {
                if (chr.getInventory(MapleInventoryType.ETC).getNumFreeSlot() <= etc)
                {
                    return false;
                }
            }
            else if (mit == MapleInventoryType.CASH)
            {
                if (chr.getInventory(MapleInventoryType.CASH).getNumFreeSlot() <= cash)
                {
                    return false;
                }
            }
        }
        return true;
    }

    public void broadcastToVisitorsThreadsafe(final byte[] packet)
    {
        synchronized (visitors)
        {
            broadcastToVisitors(packet);
        }
    }

    public void broadcastToVisitors(final byte[] packet)
    {
        for (MapleCharacter visitor : visitors)
        {
            if (visitor != null)
            {
                visitor.getClient().announce(packet);
            }
        }
    }

    public void addVisitor(MapleCharacter visitor)
    {
        synchronized (visitors)
        {
            int i = this.getFreeSlot();
            if (i > -1)
            {
                visitors[i] = visitor;
                broadcastToVisitors(MaplePacketCreator.hiredMerchantVisitorAdd(visitor, i + 1));
            }
        }
    }

    public void removeVisitor(MapleCharacter visitor)
    {
        synchronized (visitors)
        {
            int slot = getVisitorSlot(visitor);

            if (slot < 0)
            { //Not found
                return;
            }

            if (visitors[slot] != null && visitors[slot].getId() == visitor.getId())
            {
                visitors[slot] = null;

                if (slot != -1)
                {
                    broadcastToVisitors(MaplePacketCreator.hiredMerchantVisitorLeave(slot + 1));
                }
            }
        }
    }

    public int getVisitorSlotThreadsafe(MapleCharacter visitor)
    {
        synchronized (visitors)
        {
            return getVisitorSlot(visitor);
        }
    }

    public int getVisitorSlot(MapleCharacter visitor)
    {
        for (int i = 0; i < 3; i++)
        {
            if (visitors[i] != null && visitors[i].getId() == visitor.getId())
            {
                return i;
            }
        }
        return -1; //Actually 0 because of the +1's.
    }

    public void removeAllVisitors(String message)
    {
        synchronized (visitors)
        {
            for (int i = 0; i < 3; i++)
            {
                if (visitors[i] != null)
                {
                    visitors[i].setHiredMerchant(null);
                    visitors[i].getClient().announce(MaplePacketCreator.leaveHiredMerchant(i + 1, 0x11));

                    if (message.length() > 0)
                    {
                        visitors[i].dropMessage(1, message);
                    }
                    visitors[i] = null;
                }
            }
        }
    }

    public void buy(MapleClient client, int item, short quantity)
    {
        synchronized (items)
        {
            MaplePlayerShopItem pItem = items.get(item);

            Item newItem = pItem.getItem().copy();
            newItem.setQuantity((short) ((pItem.getItem().getQuantity() * quantity)));
            if ((newItem.getFlag() & ItemConstants.KARMA) == ItemConstants.KARMA)
            {
                newItem.setFlag((byte) (newItem.getFlag() ^ ItemConstants.KARMA));
            }
            if (quantity < 1 || pItem.getBundles() < 1 || !pItem.isExist() || pItem.getBundles() < quantity)
            {
                client.announce(MaplePacketCreator.enableActions());
                return;
            }
            else if (newItem.getType() == 1 && newItem.getQuantity() > 1)
            {
                client.announce(MaplePacketCreator.enableActions());
                return;
            }
            else if (!pItem.isExist())
            {
                client.announce(MaplePacketCreator.enableActions());
                return;
            }
            int price = (int) Math.min((long) pItem.getPrice() * quantity, Integer.MAX_VALUE);
            if (client.getPlayer().getMeso() >= price)
            {
                if (MapleInventoryManipulator.addFromDrop(client, newItem, true))
                {
                    client.getPlayer().gainMeso(-price, false);

                    synchronized (sold)
                    {
                        sold.add(new SoldItem(client.getPlayer().getName(), pItem.getItem().getItemId(), quantity, price));
                    }

                    pItem.setBundles((short) (pItem.getBundles() - quantity));
                    if (pItem.getBundles() < 1)
                    {
                        pItem.setDoesExist(false);
                    }
                    MapleCharacter owner = Server.getInstance().getWorld(world).getPlayerStorage().getCharacterByName(ownerName);
                    if (owner != null)
                    {
                        owner.addMerchantMesos(price);
                    }
                    else
                    {
                        try
                        {
                            Connection con = DatabaseConnection.getConnection();

                            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET MerchantMesos = MerchantMesos + " + price + " WHERE id = ?", Statement.RETURN_GENERATED_KEYS))
                            {
                                ps.setInt(1, ownerId);
                                ps.executeUpdate();
                            }

                            con.close();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                else
                {
                    client.getPlayer().dropMessage(1, "Your inventory is full. Please clean a slot before buying this item.");
                }
            }
            else
            {
                client.getPlayer().dropMessage(1, "You do not have enough mesos.");
            }
            try
            {
                this.saveItems(false);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void forceClose()
    {
        Server.getInstance().getWorld(world).unregisterHiredMerchant(this);

        try
        {
            saveItems(true);

            synchronized (items)
            {
                items.clear();
            }
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }

        map.broadcastMessage(MaplePacketCreator.destroyHiredMerchant(getOwnerId()));

        map.removeMapObject(this);

        MapleCharacter player = Server.getInstance().getWorld(world).getPlayerStorage().getCharacterById(ownerId);

        if (player != null)
        {
            player.setHasMerchant(false);
        }
        else
        {
            try
            {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE characters SET HasMerchant = 0 WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, ownerId);
                ps.executeUpdate();

                ps.close();
                con.close();
            }
            catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }

        map = null;
    }

    public void closeShop(MapleClient client, boolean timeout)
    {
        map.removeMapObject(this);
        map.broadcastMessage(MaplePacketCreator.destroyHiredMerchant(ownerId));
        client.getChannelServer().removeHiredMerchant(ownerId);

        try
        {
            MapleCharacter player = client.getWorldServer().getPlayerStorage().getCharacterById(ownerId);
            if (player != null)
            {
                player.setHasMerchant(false);
            }
            else
            {
                Connection con = DatabaseConnection.getConnection();
                try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET HasMerchant = 0 WHERE id = ?", Statement.RETURN_GENERATED_KEYS))
                {
                    ps.setInt(1, ownerId);
                    ps.executeUpdate();
                }
                con.close();
            }

            List<MaplePlayerShopItem> copyItems = getItems();

            if (check(client.getPlayer(), copyItems) && !timeout)
            {
                for (MaplePlayerShopItem mpsi : copyItems)
                {
                    if (mpsi.isExist() && (mpsi.getItem().getType() == MapleInventoryType.EQUIP.getType()))
                    {
                        MapleInventoryManipulator.addFromDrop(client, mpsi.getItem(), false);
                    }
                    else if (mpsi.isExist())
                    {
                        MapleInventoryManipulator.addById(client, mpsi.getItem().getItemId(), (short) (mpsi.getBundles() * mpsi.getItem().getQuantity()), null, -1, mpsi.getItem().getFlag(), mpsi.getItem().getExpiration());
                    }
                }

                synchronized (items)
                {
                    items.clear();
                }
            }

            try
            {
                this.saveItems(timeout);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            synchronized (items)
            {
                items.clear();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Server.getInstance().getWorld(world).unregisterHiredMerchant(this);
    }

    public String getOwner()
    {
        return ownerName;
    }

    public void clearItems()
    {
        synchronized (items)
        {
            items.clear();
        }
    }

    public int getOwnerId()
    {
        return ownerId;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public MapleCharacter[] getVisitors()
    {
        synchronized (visitors)
        {
            MapleCharacter[] copy = new MapleCharacter[3];
            System.arraycopy(visitors, 0, copy, 0, visitors.length);

            return copy;
        }
    }

    public List<MaplePlayerShopItem> getItems()
    {
        synchronized (items)
        {
            return Collections.unmodifiableList(items);
        }
    }

    public void addItem(MaplePlayerShopItem item)
    {
        synchronized (items)
        {
            items.add(item);
        }

        try
        {
            this.saveItems(false);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    public void removeFromSlot(int slot)
    {
        synchronized (items)
        {
            items.remove(slot);
        }

        try
        {
            this.saveItems(false);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    public int getFreeSlotThreadSafe()
    {
        synchronized (visitors)
        {
            return getFreeSlot();
        }
    }

    public void sendMessage(MapleCharacter chr, String msg)
    {
        String message = chr.getName() + " : " + msg;
        byte slot = (byte) (getVisitorSlot(chr) + 1);

        synchronized (messages)
        {
            messages.add(new Pair<>(message, slot));
        }
        broadcastToVisitors(MaplePacketCreator.hiredMerchantChat(message, slot));
    }

    private int getFreeSlot()
    {
        for (int i = 0; i < 3; i++)
        {
            if (visitors[i] == null)
            {
                return i;
            }
        }
        return -1;
    }

    public boolean isOpen()
    {
        return open;
    }

    public void setOpen(boolean set)
    {
        this.open = set;
    }

    public int getItemId()
    {
        return itemId;
    }

    public boolean isOwner(MapleCharacter chr)
    {
        return chr.getId() == ownerId;
    }

    public void saveItems(boolean shutdown) throws SQLException
    {
        List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();
        List<Short> bundles = new ArrayList<>();

        for (MaplePlayerShopItem pItems : getItems())
        {
            Item newItem = pItems.getItem();

            short newBundle = pItems.getBundles();

            if (shutdown)
            {
                newItem.setQuantity(pItems.getItem().getQuantity());
            }
            else
            {
                newItem.setQuantity(pItems.getItem().getQuantity());
            }

            if (newBundle > 0)
            {
                itemsWithType.add(new Pair<>(newItem, MapleInventoryType.getByType(newItem.getType())));
            }
        }

        Connection con = DatabaseConnection.getConnection();
        ItemFactory.MERCHANT.saveItems(itemsWithType, bundles, this.ownerId, con);
        con.close();
    }

    public void clearMessages()
    {
        synchronized (messages)
        {
            messages.clear();
        }
    }

    public int getChannel()
    {
        return channel;
    }

    public int getTimeLeft()
    {
        return (int) ((System.currentTimeMillis() - start) / 1000);
    }

    public List<Pair<String, Byte>> getMessages()
    {
        synchronized (messages)
        {
            List<Pair<String, Byte>> msgList = new LinkedList<>();
            msgList.addAll(messages);

            return msgList;
        }
    }

    public int getMapId()
    {
        return map.getId();
    }

    public List<SoldItem> getSold()
    {
        synchronized (sold)
        {
            return Collections.unmodifiableList(sold);
        }
    }

    public int getMesos()
    {
        return mesos;
    }

    @Override
    public void sendDestroyData(MapleClient client)
    {
    }

    @Override
    public MapleMapObjectType getType()
    {
        return MapleMapObjectType.HIRED_MERCHANT;
    }

    @Override
    public void sendSpawnData(MapleClient client)
    {
        client.announce(MaplePacketCreator.spawnHiredMerchant(this));
    }

    public class SoldItem
    {

        int itemid, mesos;
        short quantity;
        String buyer;

        public SoldItem(String buyer, int itemid, short quantity, int mesos)
        {
            this.buyer = buyer;
            this.itemid = itemid;
            this.quantity = quantity;
            this.mesos = mesos;
        }

        public String getBuyer()
        {
            return buyer;
        }

        public int getItemId()
        {
            return itemid;
        }

        public short getQuantity()
        {
            return quantity;
        }

        public int getMesos()
        {
            return mesos;
        }
    }
}
