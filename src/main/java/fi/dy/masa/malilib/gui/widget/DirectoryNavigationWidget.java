package fi.dy.masa.malilib.gui.widget;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.OpenGlHelper;
import fi.dy.masa.malilib.gui.BaseScreen;
import fi.dy.masa.malilib.gui.TextInputScreen;
import fi.dy.masa.malilib.gui.icon.DefaultIcons;
import fi.dy.masa.malilib.gui.icon.FileBrowserIconProvider;
import fi.dy.masa.malilib.gui.icon.FileBrowserIconProvider.FileBrowserIconType;
import fi.dy.masa.malilib.gui.icon.MultiIcon;
import fi.dy.masa.malilib.gui.position.HorizontalAlignment;
import fi.dy.masa.malilib.gui.util.GuiUtils;
import fi.dy.masa.malilib.gui.util.ScreenContext;
import fi.dy.masa.malilib.gui.widget.button.GenericButton;
import fi.dy.masa.malilib.gui.widget.util.DirectoryNavigator;
import fi.dy.masa.malilib.listener.EventListener;
import fi.dy.masa.malilib.listener.TextChangeListener;
import fi.dy.masa.malilib.render.ShapeRenderUtils;
import fi.dy.masa.malilib.util.DirectoryCreator;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.malilib.util.data.LeftRight;

public class DirectoryNavigationWidget extends SearchBarWidget
{
    protected final DirectoryNavigator navigator;
    protected final FileBrowserIconProvider iconProvider;
    protected final GenericButton buttonRoot;
    protected final GenericButton buttonUp;
    protected final GenericButton buttonCreateDir;
    protected final InfoIconWidget infoWidget;
    protected final File rootDir;
    @Nullable protected final Supplier<String> rootDirDisplayNameSupplier;
    protected File currentDir;
    protected int pathStartX;

    public DirectoryNavigationWidget(int x, int y, int width, int height,
                                     File currentDir, File rootDir, DirectoryNavigator navigator,
                                     FileBrowserIconProvider iconProvider,
                                     TextChangeListener textChangeListener,
                                     @Nullable EventListener openCloseListener)
    {
        this(x, y, width, height, currentDir, rootDir, navigator, iconProvider,
             textChangeListener, openCloseListener, null);
    }

    public DirectoryNavigationWidget(int x, int y, int width, int height,
                                     File currentDir, File rootDir, DirectoryNavigator navigator,
                                     FileBrowserIconProvider iconProvider,
                                     TextChangeListener textChangeListener,
                                     @Nullable EventListener openCloseListener,
                                     @Nullable Supplier<String> rootDirDisplayNameSupplier)
    {
        super(x, y, width, height, 0, iconProvider.getIcon(FileBrowserIconType.SEARCH),
              HorizontalAlignment.RIGHT, textChangeListener, openCloseListener);

        this.currentDir = currentDir;
        this.rootDir = rootDir;
        this.navigator = navigator;
        this.iconProvider = iconProvider;
        this.rootDirDisplayNameSupplier = rootDirDisplayNameSupplier;

        this.buttonRoot = GenericButton.createIconOnly(iconProvider.getIcon(FileBrowserIconType.ROOT));
        this.buttonRoot.translateAndAddHoverStrings("malilib.gui.button.hover.directory_widget.root");
        this.buttonRoot.setPlayClickSound(false);
        this.buttonRoot.setActionListener(() -> { if (this.searchOpen == false) this.navigator.switchToRootDirectory(); });

        this.buttonUp = GenericButton.createIconOnly(iconProvider.getIcon(FileBrowserIconType.UP));
        this.buttonUp.translateAndAddHoverStrings("malilib.gui.button.hover.directory_widget.up");
        this.buttonUp.setPlayClickSound(false);
        this.buttonUp.setActionListener(() -> { if (this.searchOpen == false) this.navigator.switchToParentDirectory(); });

        this.buttonCreateDir = GenericButton.createIconOnly(iconProvider.getIcon(FileBrowserIconType.CREATE_DIR));
        this.buttonCreateDir.translateAndAddHoverStrings("malilib.gui.button.hover.directory_widget.create_directory");
        this.buttonCreateDir.setPlayClickSound(false);
        this.buttonCreateDir.setActionListener(() -> {
            if (this.searchOpen == false)
            {
                DirectoryCreator creator = new DirectoryCreator(this.getCurrentDirectory(), this.navigator);
                TextInputScreen gui = new TextInputScreen("malilib.gui.title.create_directory", "", GuiUtils.getCurrentScreen(), creator);
                BaseScreen.openPopupScreen(gui);
            }
        });

        this.infoWidget = new InfoIconWidget(DefaultIcons.INFO_ICON_11, "malilib.gui.button.hover.directory_widget.hold_shift_to_open_directory");
    }

    @Override
    public void reAddSubWidgets()
    {
        super.reAddSubWidgets();

        if (this.isSearchOpen() == false)
        {
            this.addWidget(this.buttonRoot);
            this.addWidget(this.buttonUp);
            this.addWidget(this.buttonCreateDir);
            this.addWidget(this.infoWidget);

            this.placePathElements(this.pathStartX, this.getY(), this.generatePathElements());
        }
    }

    @Override
    public void updateSubWidgetsToGeometryChanges()
    {
        super.updateSubWidgetsToGeometryChanges();

        int x = this.getX() + 2;
        int y = this.getY();
        int height = this.getHeight();
        int middleY = y + height / 2;

        int by = middleY - this.buttonRoot.getHeight() / 2;
        this.buttonRoot.setPosition(x, by);

        x += this.buttonRoot.getWidth() + 2;
        this.buttonUp.setPosition(x, by);

        x += this.buttonUp.getWidth() + 2;
        this.buttonCreateDir.setPosition(x, by);

        int xRight = this.getRight();
        int iw = this.infoWidget.getWidth();
        int ih = this.infoWidget.getHeight();
        int tw = this.buttonSearchToggle.getWidth();

        this.buttonSearchToggle.setPosition(xRight - tw - iw - 4, by);
        this.infoWidget.setPosition(xRight - iw - 2, middleY - ih / 2 - 1);

        this.textField.setWidth(this.getWidth() - tw - iw - 8);
        this.pathStartX = this.buttonCreateDir.getRight() + 6;

        this.reAddSubWidgets();
    }

    public File getCurrentDirectory()
    {
        return this.currentDir;
    }

    public void setCurrentDirectory(File dir)
    {
        this.currentDir = dir;
        this.reAddSubWidgets();
    }

    public MultiIcon getNavBarIconRoot(boolean isOpen)
    {
        return this.iconProvider.getIcon(isOpen ? FileBrowserIconType.NAVBAR_ROOT_PATH_OPEN : FileBrowserIconType.NAVBAR_ROOT_PATH_CLOSED);
    }

    public MultiIcon getNavBarIconSubdirs(boolean isOpen)
    {
        return this.iconProvider.getIcon(isOpen ? FileBrowserIconType.NAVBAR_SUBDIRS_OPEN : FileBrowserIconType.NAVBAR_SUBDIRS_CLOSED);
    }

    protected int getMaxPathBarWidth()
    {
        return this.getWidth() - this.pathStartX - 30;
    }

    @Override
    public void renderAt(int x, int y, float z, ScreenContext ctx)
    {
        if (this.searchOpen == false)
        {
            int diffX = x - this.getX();
            int rx = this.pathStartX - 2 + diffX;
            ShapeRenderUtils.renderRectangle(rx, y, z, this.getMaxPathBarWidth() + 4, this.getHeight(), 0xFF242424);
        }

        super.renderAt(x, y, z, ctx);
    }

    protected String getDisplayNameForDirectory(File dir)
    {
        String name;

        if (this.rootDirDisplayNameSupplier != null && this.rootDir.equals(dir))
        {
            name = this.rootDirDisplayNameSupplier.get();
        }
        else
        {
            name = dir.getName();
        }

        // The partition root on windows returns an empty string... ('C:\' -> '')
        if (name.length() == 0)
        {
            name = dir.toString();
        }

        return name;
    }

    protected void placePathElements(int x, int y, List<PathElement> elements)
    {
        Function<File, String> displayNameFactory = this::getDisplayNameForDirectory;
        int bh = 14;
        int by = y + this.getHeight() / 2 - bh / 2;

        for (final PathElement el : elements)
        {
            if (el.type == PathElement.Type.DIR)
            {
                GenericButton button = new GenericButton(el.nameWidth + 4, bh, el.displayName);
                button.setPosition(x, by);
                button.setPlayClickSound(false);
                button.setHorizontalLabelPadding(2);
                button.setRenderButtonBackgroundTexture(false);
                button.getBorderRenderer().getHoverSettings().setEnabled(true);
                button.getTextSettings().setTextShadowEnabled(false);
                button.setDefaultHoveredTextColor(0xFFFFFFFF);
                button.setActionListener(() -> {
                    if (BaseScreen.isShiftDown())
                    {
                        OpenGlHelper.openFile(el.dir);
                    }
                    else
                    {
                        this.navigator.switchToDirectory(el.dir);
                    }
                });

                this.addWidget(button);

                List<File> dirs = FileUtils.getSubDirectories(el.dir);

                if (dirs.isEmpty() == false)
                {
                    final DropDownListWidget<File> dropdown = new DropDownListWidget<>(-1, 12, 120, 10, dirs, displayNameFactory);
                    dropdown.setY(y + 16);
                    dropdown.setRight(x + el.totalWidth);
                    dropdown.setNoBarWhenClosed(x + el.totalWidth - 12, y + 3, () -> this.getNavBarIconSubdirs(dropdown.isOpen()));
                    dropdown.setSelectionListener(this.navigator::switchToDirectory);
                    this.addWidget(dropdown);
                }
            }
            else
            {
                List<File> dirs = FileUtils.getDirsForRootPath(el.dir, this.rootDir);
                final DropDownListWidget<File> dropdown = new DropDownListWidget<>(-1, 12, 120, 10, dirs, displayNameFactory);
                dropdown.setPosition(x, y + 16);
                dropdown.setNoBarWhenClosed(x, y + 2, () -> this.getNavBarIconRoot(dropdown.isOpen()));
                dropdown.setSelectionListener(this.navigator::switchToDirectory);
                this.addWidget(dropdown);
            }

            x += el.totalWidth;
        }
    }

    protected List<PathElement> generatePathElements()
    {
        ArrayList<PathElement> list = new ArrayList<>();
        File root = this.rootDir;
        File dir = this.currentDir;
        int maxWidth = this.getMaxPathBarWidth();
        int usedWidth = 0;
        int adjDirsIconWidth = this.getNavBarIconSubdirs(false).getWidth();

        while (dir != null)
        {
            String name = this.getDisplayNameForDirectory(dir);
            int nameWidth = this.getStringWidth(name);
            int entryWidth = nameWidth + adjDirsIconWidth + 8;

            // The current path element doesn't fit.
            // This thus also means that all the elements didn't fit, and they will be cut from the beginning.
            // This also means that the root paths dropdown widget should be shown.
            if (usedWidth + entryWidth > maxWidth)
            {
                // This is the current directory, show as much of it as possible
                if (usedWidth == 0)
                {
                    name = StringUtils.clampTextToRenderLength(name, maxWidth - adjDirsIconWidth, LeftRight.RIGHT, "...");
                    list.add(new PathElement(dir, PathElement.Type.DIR, name, nameWidth, entryWidth));

                    if (root.equals(dir) == false)
                    {
                        dir = dir.getParentFile();
                    }
                }

                if (dir != null)
                {
                    int w = this.getNavBarIconRoot(false).getWidth() + 4;
                    list.add(new PathElement(dir, PathElement.Type.ROOT_PATH, "", w, w));
                }

                break;
            }

            list.add(new PathElement(dir, PathElement.Type.DIR, name, nameWidth, entryWidth));

            usedWidth += entryWidth;

            if (root.equals(dir))
            {
                break;
            }

            dir = dir.getParentFile();
        }

        Collections.reverse(list);

        return list;
    }

    public static class PathElement
    {
        public final File dir;
        public final Type type;
        public final String displayName;
        public final int nameWidth;
        public final int totalWidth;

        public PathElement(File dir, Type type, String displayName, int nameWidth, int totalWidth)
        {
            this.dir = dir;
            this.type = type;
            this.displayName = displayName;
            this.nameWidth = nameWidth;
            this.totalWidth = totalWidth;
        }

        public enum Type
        {
            ROOT_PATH,
            DIR
        }
    }
}
