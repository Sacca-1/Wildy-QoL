package com.wildyqol;

import com.google.inject.Provides;
import com.wildyqol.freezetimers.ExtendedFreezeTimersService;
import com.wildyqol.ikodparchmentrisk.IkodParchmentRiskOverlay;
import com.wildyqol.menaphite.MenaphiteProcInfoBox;
import com.wildyqol.menaphite.MenaphiteProcStatusBarOverlay;
import com.wildyqol.menaphite.MenaphiteProcTimerService;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority;
import net.runelite.client.util.Text;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.game.SpriteManager;



@Slf4j
@PluginDescriptor(
        name = "Wildy QoL",
        description = "Quality of life improvements for wilderness activities"
)
public class WildyQoLPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private ConfigManager configManager;

    @Inject
    private WildyQoLConfig config;

    @Inject
    private ChatMessageManager chatMessageManager;

    private boolean shouldShowUpdateMessage = false;

    @Inject
    private ItemManager itemManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private IkodParchmentRiskOverlay ikodParchmentRiskOverlay;

    @Inject
    private MenaphiteProcTimerService menaphiteProcTimerService;

    @Inject
    private InfoBoxManager infoBoxManager;

    @Inject
    private MenaphiteProcStatusBarOverlay menaphiteProcStatusBarOverlay;

    @Inject
    private FishInventoryIconOverlay fishInventoryIconOverlay;

    @Inject
    private SpriteManager spriteManager;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ExtendedFreezeTimersService extendedFreezeTimersService;

    private ProtectItemInfoBox protectItemInfoBox;

    private static final Set<Integer> RUNE_POUCH_ITEM_IDS = Set.of(
        ItemID.BH_RUNE_POUCH,
        ItemID.BH_RUNE_POUCH_TROUVER,
        ItemID.DIVINE_RUNE_POUCH,
        ItemID.DIVINE_RUNE_POUCH_TROUVER
    );

    private static final long TROUVER_REPARCH_COST = 500_000L;

    private MenaphiteProcInfoBox menaphiteProcInfoBox;
    private BufferedImage menaphiteImage;
    private boolean menaphiteStatusBarOverlayAdded;

    @Override
    protected void startUp()
    {
        log.debug("Pet Spell Blocker enabled: {}", config.petSpellBlocker());
        log.debug("Empty Vial Blocker enabled: {}", config.emptyVialBlocker());

        overlayManager.add(ikodParchmentRiskOverlay);
        overlayManager.add(fishInventoryIconOverlay);
        removeMenaphiteProcInfoBox();
        removeMenaphiteStatusBarOverlay();
        menaphiteImage = loadMenaphiteImage();
        menaphiteProcStatusBarOverlay.setMenaphiteImage(menaphiteImage);
        menaphiteProcTimerService.reset();
        updateMenaphiteStatusBarOverlay();

        clientThread.invokeLater(() ->
        {
            updateTrouverSurcharge();
        });

        protectItemInfoBox = new ProtectItemInfoBox(config, client, this, spriteManager);
        infoBoxManager.addInfoBox(protectItemInfoBox);

        extendedFreezeTimersService.startUp(this);

        // Check if we should show update message (but don't show it yet)
        if (!config.updateMessageShown130())
        {
            shouldShowUpdateMessage = true;
        }
    }

    @Override
    protected void shutDown()
    {
        log.debug("Wildy QoL stopped");
        overlayManager.remove(ikodParchmentRiskOverlay);
        overlayManager.remove(fishInventoryIconOverlay);
        ikodParchmentRiskOverlay.reset();
        menaphiteProcTimerService.reset();
        removeMenaphiteProcInfoBox();
        removeMenaphiteStatusBarOverlay();
        menaphiteProcStatusBarOverlay.clearMenaphiteImage();
        menaphiteImage = null;
        infoBoxManager.removeInfoBox(protectItemInfoBox);
        protectItemInfoBox = null;
        extendedFreezeTimersService.shutDown();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        extendedFreezeTimersService.onGameStateChanged(gameStateChanged);

        if (gameStateChanged.getGameState() != GameState.LOGGED_IN)
        {
            menaphiteProcTimerService.reset();
            removeMenaphiteProcInfoBox();
            removeMenaphiteStatusBarOverlay();
            return;
        }

        // Show update message if needed
        if (shouldShowUpdateMessage)
        {
            showUpdateMessage();
            configManager.setConfiguration("wildyqol", "updateMessageShown130", true);
            shouldShowUpdateMessage = false;
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {

        if (config.petSpellBlocker())
        {
            handlePetSpellBlock(event);
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (handleRunePouchLeftClick(event))
        {
            return;
        }

        handleEmptyVialBlocker(event);
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event)
    {
        if (event.getGroupId() == InterfaceID.DEATHKEEP)
        {
            updateTrouverSurcharge();
        }
    }

    @Subscribe
    public void onWidgetClosed(WidgetClosed event)
    {
        if (event.getGroupId() == InterfaceID.DEATHKEEP)
        {
            ikodParchmentRiskOverlay.reset();
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        if (isIkodOpen())
        {
            updateTrouverSurcharge();
        }

        extendedFreezeTimersService.onItemContainerChanged(event);
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event)
    {
        if (isIkodOpen())
        {
            updateTrouverSurcharge();
        }

        if (event.getVarbitId() == VarbitID.STATRENEWAL_POTION_TIMER)
        {
            handleMenaphiteVarbit(event.getValue());
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (!"wildyqol".equals(event.getGroup()))
        {
            return;
        }

        if ("enableExtendedFreezeTimersV2".equals(event.getKey()))
        {
            extendedFreezeTimersService.onConfigChanged();
        }

        if ("menaphiteProcTimerShowInfoBox".equals(event.getKey()))
        {
            if (!config.menaphiteProcTimerShowInfoBox())
            {
                removeMenaphiteProcInfoBox();
            }
            else if (menaphiteProcTimerService.isActive())
            {
                ensureMenaphiteProcInfoBox();
            }

            clientThread.invokeLater(() ->
            {
                int varbitValue = client.getVarbitValue(VarbitID.STATRENEWAL_POTION_TIMER);
                handleMenaphiteVarbit(varbitValue);
            });
        }

        if ("menaphiteProcTimerStatusBarMode".equals(event.getKey()))
        {
            clientThread.invokeLater(() ->
            {
                int varbitValue = client.getVarbitValue(VarbitID.STATRENEWAL_POTION_TIMER);
                handleMenaphiteVarbit(varbitValue);
            });
        }


    }

    @Subscribe
    public void onGraphicChanged(GraphicChanged event)
    {
        extendedFreezeTimersService.onGraphicChanged(event);
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        extendedFreezeTimersService.onGameTick(event);
    }

    @Subscribe
    public void onInteractingChanged(InteractingChanged event)
    {
        extendedFreezeTimersService.onInteractingChanged(event);
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event)
    {
        extendedFreezeTimersService.onAnimationChanged(event);
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event)
    {
        extendedFreezeTimersService.onHitsplatApplied(event);
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        extendedFreezeTimersService.onChatMessage(event);
    }

    @Subscribe
    public void onPlayerDespawned(PlayerDespawned event)
    {
        extendedFreezeTimersService.onPlayerDespawned(event);
    }



    private void handlePetSpellBlock(MenuEntryAdded event)
    {
        boolean isCastOption = "Cast".equals(event.getOption());
        boolean isExamineNpc = event.getMenuEntry().getType() == MenuAction.EXAMINE_NPC;
        boolean isSpellSelected = isSpellSelected();

        // Block spell casts and suppress pet examine while a spell is selected.
        if (!isCastOption && (!isSpellSelected || !isExamineNpc))
        {
            return;
        }

        // Get the target NPC
        int npcIndex = event.getIdentifier();
        if (npcIndex < 0)
        {
            return;
        }

        // Get NPC from the scene using the new API
        NPC npc = client.getTopLevelWorldView().npcs().byIndex(npcIndex);
        
        if (npc == null)
        {
            return;
        }

        // Check if the NPC is a follower (pet)
        NPCComposition comp = npc.getComposition();
        if (comp != null && comp.isFollower())
        {
            // Remove the menu entry using the new API
            // Use the new Menu API to remove the entry
            client.getMenu().removeMenuEntry(event.getMenuEntry());
            log.debug("Removed spell cast menu entry for pet: {}", npc.getName());
        }
    }

    private boolean isSpellSelected()
    {
        if (!client.isWidgetSelected())
        {
            return false;
        }

        Widget selectedWidget = client.getSelectedWidget();
        return selectedWidget != null
            && WidgetUtil.componentToInterface(selectedWidget.getId()) == InterfaceID.MAGIC_SPELLBOOK;
    }

    private boolean handleRunePouchLeftClick(MenuOptionClicked event)
    {
        if (!config.runePouchBlocker() || client.isMenuOpen())
        {
            return false;
        }

        if (!isInPvpArea())
        {
            return false;
        }

        MenuEntry clicked = event.getMenuEntry();
        if (!isRunePouch(clicked.getItemId()) || clicked.getType() == MenuAction.EXAMINE_ITEM)
        {
            return false;
        }

        event.consume();
        return true;
    }

    private boolean isRunePouch(int itemId)
    {
        int canonicalId = itemManager.canonicalize(itemId);
        return canonicalId > 0 && RUNE_POUCH_ITEM_IDS.contains(canonicalId);
    }

    private boolean isInPvpArea()
    {
        return client.getVarbitValue(VarbitID.INSIDE_WILDERNESS) == 1
            || client.getVarbitValue(VarbitID.PVP_AREA_CLIENT) == 1;
    }


    private void handleEmptyVialBlocker(MenuOptionClicked event)
    {
        if (!config.emptyVialBlocker() || !isInPvpArea())
        {
            return;
        }

        if (!"Use".equals(event.getMenuEntry().getOption()))
        {
            return;
        }

        if (event.getMenuEntry().getItemId() == ItemID.VIAL_EMPTY)
        {
            event.consume();
        }
    }

    private void showUpdateMessage()
    {
        chatMessageManager.queue(QueuedMessage.builder()
            .type(ChatMessageType.GAMEMESSAGE)
            .runeLiteFormattedMessage("<col=00ff00>Wildy QoL:</col> v1.3.0: added remapping for new fish icons, extended freeze timers (ancient sceptres, swampbark), and rune pouch misclick prevention. Check plugin settings for details.")
            .build());
    }

    private void updateTrouverSurcharge()
    {
        if (!config.showIkodTrouverOverlay())
        {
            ikodParchmentRiskOverlay.reset();
            return;
        }

        boolean ikodOpen = isIkodOpen();
        if (!ikodOpen)
        {
            ikodParchmentRiskOverlay.reset();
            return;
        }

        Widget riskWidget = client.getWidget(InterfaceID.Deathkeep.VALUE);
        if (riskWidget == null || riskWidget.isHidden())
        {
            ikodParchmentRiskOverlay.reset();
            return;
        }

        int lockedCount = countLockedItems();
        long perItemCost = getPerItemCost();
        long surcharge = lockedCount * perItemCost;
        long baseRisk = getGuideRiskValue(riskWidget);
        Color labelColor = deriveLabelColor(riskWidget);

        ikodParchmentRiskOverlay.update(true, lockedCount, perItemCost, surcharge, baseRisk, labelColor);
    }

    private boolean isIkodOpen()
    {
        Widget root = client.getWidget(InterfaceID.Deathkeep.CONTENTS);
        return root != null && !root.isHidden();
    }

    private int countLockedItems()
    {
        Set<Widget> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<Widget> stack = new ArrayDeque<>();
        pushRoot(stack, client.getWidget(InterfaceID.Deathkeep.KEPT));
        pushRoot(stack, client.getWidget(InterfaceID.Deathkeep.GRAVE));

        if (stack.isEmpty())
        {
            pushRoot(stack, client.getWidget(InterfaceID.Deathkeep.ITEMS));
        }

        int lockedCount = 0;

        while (!stack.isEmpty())
        {
            Widget widget = stack.pop();

            if (widget == null || !visited.add(widget) || widget.isHidden())
            {
                continue;
            }

            lockedCount += countLockedItemsForWidget(widget);

            pushChildren(stack, widget.getDynamicChildren());
            pushChildren(stack, widget.getStaticChildren());
            pushChildren(stack, widget.getNestedChildren());
            pushChildren(stack, widget.getChildren());
        }

        return lockedCount;
    }

    private void pushRoot(Deque<Widget> stack, Widget widget)
    {
        if (widget != null && !widget.isHidden())
        {
            stack.push(widget);
        }
    }

    private int countLockedItemsForWidget(Widget widget)
    {
        int itemId = widget.getItemId();
        if (itemId <= 0)
        {
            return 0;
        }

        if (widget.getBorderType() != 2)
        {
            return 0;
        }

        int canonicalId = itemManager.canonicalize(itemId);
        ItemComposition composition = itemManager.getItemComposition(canonicalId);

        if (composition == null || !composition.getName().endsWith(" (l)"))
        {
            return 0;
        }

        int quantity = Math.max(1, widget.getItemQuantity());
        return quantity;
    }

    private void pushChildren(Deque<Widget> stack, Widget[] children)
    {
        if (children == null)
        {
            return;
        }

        for (Widget child : children)
        {
            if (child != null)
            {
                stack.push(child);
            }
        }
    }

    private long getPerItemCost()
    {
        long parchmentPrice = itemManager.getItemPrice(ItemID.TROUVER_PARCHMENT);
        return parchmentPrice + TROUVER_REPARCH_COST;
    }

    private long getGuideRiskValue(Widget riskWidget)
    {
        String rawText = riskWidget.getText();
        if (rawText == null)
        {
            return 0L;
        }

        String sanitized = Text.removeTags(rawText).replaceAll("[^0-9]", "");
        if (sanitized.isEmpty())
        {
            return 0L;
        }

        try
        {
            return Long.parseLong(sanitized);
        }
        catch (NumberFormatException ex)
        {
            return 0L;
        }
    }

    private Color deriveLabelColor(Widget riskWidget)
    {
        int color = riskWidget.getTextColor();
        if (color == 0)
        {
            return Color.WHITE;
        }

        return new Color(color | 0xFF000000, true);
    }

    private void handleMenaphiteVarbit(int varbitValue)
    {
        if (!isMenaphiteTimerEnabled())
        {
            menaphiteProcTimerService.reset();
            removeMenaphiteProcInfoBox();
            removeMenaphiteStatusBarOverlay();
            return;
        }

        menaphiteProcTimerService.handleVarbitUpdate(varbitValue, client.getTickCount());

        if (menaphiteProcTimerService.isActive() && config.menaphiteProcTimerShowInfoBox())
        {
            ensureMenaphiteProcInfoBox();
        }
        else
        {
            removeMenaphiteProcInfoBox();
        }

        updateMenaphiteStatusBarOverlay();
    }

    private void ensureMenaphiteProcInfoBox()
    {
        if (!config.menaphiteProcTimerShowInfoBox())
        {
            removeMenaphiteProcInfoBox();
            return;
        }

        if (menaphiteProcInfoBox != null)
        {
            return;
        }

        if (menaphiteImage == null)
        {
            menaphiteImage = loadMenaphiteImage();
        }

        menaphiteProcInfoBox = new MenaphiteProcInfoBox(menaphiteImage, this, menaphiteProcTimerService, config, client);
        menaphiteProcInfoBox.setTooltip("Time until next menaphite remedy proc");
        menaphiteProcInfoBox.setPriority(InfoBoxPriority.MED);
        infoBoxManager.addInfoBox(menaphiteProcInfoBox);
        menaphiteProcStatusBarOverlay.setMenaphiteImage(menaphiteImage);
    }

    private void removeMenaphiteProcInfoBox()
    {
        if (menaphiteProcInfoBox == null)
        {
            return;
        }

        infoBoxManager.removeInfoBox(menaphiteProcInfoBox);
        menaphiteProcInfoBox = null;
    }

    private BufferedImage loadMenaphiteImage()
    {
        BufferedImage image = itemManager.getImage(ItemID._4DOSESTATRENEWAL);
        if (image != null)
        {
            return image;
        }

        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }

    private void ensureMenaphiteStatusBarOverlay()
    {
        if (menaphiteStatusBarOverlayAdded)
        {
            return;
        }

        overlayManager.add(menaphiteProcStatusBarOverlay);
        menaphiteStatusBarOverlayAdded = true;
    }

    private void removeMenaphiteStatusBarOverlay()
    {
        if (!menaphiteStatusBarOverlayAdded)
        {
            return;
        }

        overlayManager.remove(menaphiteProcStatusBarOverlay);
        menaphiteStatusBarOverlayAdded = false;
    }

    private void updateMenaphiteStatusBarOverlay()
    {
        WildyQoLConfig.MenaphiteProcStatusBarMode mode = config.menaphiteProcTimerStatusBarMode();
        if (mode == null || mode == WildyQoLConfig.MenaphiteProcStatusBarMode.OFF)
        {
            removeMenaphiteStatusBarOverlay();
            return;
        }

        MenaphiteProcStatusBarOverlay.MenaphiteStatusBarPosition position =
            mode == WildyQoLConfig.MenaphiteProcStatusBarMode.LEFT
                ? MenaphiteProcStatusBarOverlay.MenaphiteStatusBarPosition.LEFT
                : MenaphiteProcStatusBarOverlay.MenaphiteStatusBarPosition.RIGHT;
        menaphiteProcStatusBarOverlay.setPosition(position);
        ensureMenaphiteStatusBarOverlay();
        if (menaphiteImage == null)
        {
            menaphiteImage = loadMenaphiteImage();
        }
        menaphiteProcStatusBarOverlay.setMenaphiteImage(menaphiteImage);
    }

    @Provides
    WildyQoLConfig provideConfig(ConfigManager configManager)
    {
        WildyQoLConfig cfg = configManager.getConfig(WildyQoLConfig.class);
        configManager.setDefaultConfiguration(cfg, false);
        return cfg;
    }

    private boolean isMenaphiteTimerEnabled()
    {
        WildyQoLConfig.MenaphiteProcStatusBarMode mode = config.menaphiteProcTimerStatusBarMode();
        boolean statusBarEnabled = mode != null && mode != WildyQoLConfig.MenaphiteProcStatusBarMode.OFF;
        return config.menaphiteProcTimerShowInfoBox() || statusBarEnabled;
    }


}
