package gui_menus;

import executables.stiki_frontend_driver;
import gui_panels.gui_diff_panel;
import gui_support.diff_markup;
import gui_support.gui_colorpicker;
import gui_support.gui_globals;
import gui_support.gui_settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Andrew G. West - gui_menu_options.java - This class builds the "options"
 * menu, which allows users to alter the appearance of the STiki GUI and
 * other, similar, non-mission critical selections
 */
@SuppressWarnings("serial")
public class gui_menu_options extends JMenu implements ActionListener {

	// **************************** PRIVATE FIELDS ***************************

	/**
	 * Frame which contains this menu, so that the visual aspects
	 * can be altered via menu selections.
	 */
	private stiki_frontend_driver parent;

	// A hierarchical view of the component layout of this menu
	private JMenu submenu_browser_font_size;
	private JRadioButtonMenuItem browser_font_10;
	private JRadioButtonMenuItem browser_font_12;
	private JRadioButtonMenuItem browser_font_14;
	private JRadioButtonMenuItem browser_font_16;
	private JRadioButtonMenuItem browser_font_18;
	private JRadioButtonMenuItem browser_font_20;
	private JRadioButtonMenuItem browser_font_22;
	private JRadioButtonMenuItem browser_font_24;
	private JMenu submenu_browser_font_family;
	private JRadioButtonMenuItem browser_font_serif;
	private JRadioButtonMenuItem browser_font_sans;
	private JRadioButtonMenuItem browser_font_mono;
	private JMenu submenu_browser_colors;
	private JMenuItem browser_color_bg;
	private JMenuItem browser_color_text;
	private JMenuItem browser_color_words;
	private JMenuItem browser_color_cont;
	private JMenuItem browser_color_add;
	private JMenuItem browser_color_del;
	private JMenuItem browser_color_note;
	private JMenuItem browser_color_reset;
	private JCheckBoxMenuItem xlink_cb;
	private JCheckBoxMenuItem dttr_cb;
	private JCheckBoxMenuItem agf_comment_cb;
	private JCheckBoxMenuItem aiv_popup_cb;


	// ***************************** CONSTRUCTORS ****************************

	/**
	 * Construct a [gui_menu_options] -- creating the button, its mnemonic,
	 * its submenus, the sub-menu items, and adding all necessary listeners.
	 *
	 * @param parent class which contains this menu, so that the visual aspects
	 *               can be altered via menu selections.
	 */
	public gui_menu_options(stiki_frontend_driver parent) {

		this.parent = parent; // Argument assignment

		// First set properties of top-level menu item
		this.setText("Options");
		this.setFont(gui_globals.PLAIN_NORMAL_FONT);
		this.setMnemonic(KeyEvent.VK_T);

		// Intialize sub-menus and items, add them to top-level
		initialize_subitems();
		this.add(submenu_browser_font_size);
		this.add(submenu_browser_font_family);
		this.add(submenu_browser_colors);
		this.add(xlink_cb);
		this.add(dttr_cb);
		this.add(agf_comment_cb);
		this.add(aiv_popup_cb);

		// Set default menu selections (per persistent settings)
		this.selected_browser_font_size(gui_settings.get_int_def(
				gui_settings.SETTINGS_INT.options_fontsize,
				gui_globals.DEFAULT_BROWSER_FONT.getSize()));
		this.selected_browser_font_fam(gui_settings.get_str_def(
				gui_settings.SETTINGS_STR.options_fontfam,
				gui_globals.DEFAULT_BROWSER_FONT.getFamily()));
		this.set_hyperlink_policy(gui_settings.get_bool_def(
				gui_settings.SETTINGS_BOOL.options_hyperlinks,
				parent.diff_browser.get_hyperlink_policy()));
		this.set_dttr_policy(gui_settings.get_bool_def(
				gui_settings.SETTINGS_BOOL.options_dttr, true));
		this.set_agf_comment_policy(gui_settings.get_bool_def(
				gui_settings.SETTINGS_BOOL.options_agf_comment, true));
		this.set_aiv_popup_policy(gui_settings.get_bool_def(
				gui_settings.SETTINGS_BOOL.options_aiv_popup, false));
	}


	// **************************** PUBLIC METHODS ***************************

	/**
	 * Overriding: Map menu-item selections to opening of help pane/dialog.
	 */
	@Override
	public void actionPerformed(ActionEvent event) {

		if (event.getSource().equals(xlink_cb)) {
			set_hyperlink_policy(xlink_cb.isSelected());
		} else if (event.getSource().equals(dttr_cb)) {

			// Again, nothing. Other classes will check-in with this class
			// as needed to determine option status

		} else if (event.getSource().equals(agf_comment_cb)) {

			// Do nothing

		} else if (event.getSource().equals(aiv_popup_cb)) {

			// Do nothing

		} else if (event.getSource().equals(browser_font_10) ||
				event.getSource().equals(browser_font_12) ||
				event.getSource().equals(browser_font_14) ||
				event.getSource().equals(browser_font_16) ||
				event.getSource().equals(browser_font_18) ||
				event.getSource().equals(browser_font_20) ||
				event.getSource().equals(browser_font_22) ||
				event.getSource().equals(browser_font_24)) {

			int browser_font_size = -1;
			if (event.getSource().equals(browser_font_10))
				browser_font_size = 10;
			else if (event.getSource().equals(browser_font_12))
				browser_font_size = 12;
			else if (event.getSource().equals(browser_font_14))
				browser_font_size = 14;
			else if (event.getSource().equals(browser_font_16))
				browser_font_size = 16;
			else if (event.getSource().equals(browser_font_18))
				browser_font_size = 18;
			else if (event.getSource().equals(browser_font_20))
				browser_font_size = 20;
			else if (event.getSource().equals(browser_font_22))
				browser_font_size = 22;
			else if (event.getSource().equals(browser_font_24))
				browser_font_size = 24;

			// Pass off size change to handler
			this.selected_browser_font_size(browser_font_size);

		} else if (event.getSource().equals(browser_font_serif) ||
				event.getSource().equals(browser_font_sans) ||
				event.getSource().equals(browser_font_mono)) {

			String browser_font_fam = "";
			if (event.getSource().equals(browser_font_serif))
				browser_font_fam = Font.SERIF;
			else if (event.getSource().equals(browser_font_sans))
				browser_font_fam = Font.SANS_SERIF;
			else if (event.getSource().equals(browser_font_mono))
				browser_font_fam = Font.MONOSPACED;

			// Pass off family change to handler
			this.selected_browser_font_fam(browser_font_fam);

		} else {

			try {
				if (event.getSource().equals(browser_color_bg)) {
					diff_markup.COLOR_DIFF_BG = gui_colorpicker.
							dialog_response(parent, diff_markup.COLOR_DIFF_BG);
					browser_color_bg.setIcon(new gui_color_icon(
							gui_globals.hex_to_rgb(diff_markup.COLOR_DIFF_BG)));

				} else if (event.getSource().equals(browser_color_text)) {
					diff_markup.COLOR_DIFF_TEXT = gui_colorpicker.
							dialog_response(parent, diff_markup.COLOR_DIFF_TEXT);
					browser_color_text.setIcon(new gui_color_icon(
							gui_globals.hex_to_rgb(diff_markup.COLOR_DIFF_TEXT)));

				} else if (event.getSource().equals(browser_color_words)) {
					diff_markup.COLOR_DIFF_WORDS = gui_colorpicker.
							dialog_response(parent, diff_markup.COLOR_DIFF_WORDS);
					browser_color_words.setIcon(new gui_color_icon(
							gui_globals.hex_to_rgb(diff_markup.COLOR_DIFF_WORDS)));

				} else if (event.getSource().equals(browser_color_cont)) {
					diff_markup.COLOR_DIFF_CONT = gui_colorpicker.
							dialog_response(parent, diff_markup.COLOR_DIFF_CONT);
					browser_color_cont.setIcon(new gui_color_icon(
							gui_globals.hex_to_rgb(diff_markup.COLOR_DIFF_CONT)));

				} else if (event.getSource().equals(browser_color_add)) {
					diff_markup.COLOR_DIFF_ADD = gui_colorpicker.
							dialog_response(parent, diff_markup.COLOR_DIFF_ADD);
					browser_color_add.setIcon(new gui_color_icon(
							gui_globals.hex_to_rgb(diff_markup.COLOR_DIFF_ADD)));

				} else if (event.getSource().equals(browser_color_del)) {
					diff_markup.COLOR_DIFF_DEL = gui_colorpicker.
							dialog_response(parent, diff_markup.COLOR_DIFF_DEL);
					browser_color_del.setIcon(new gui_color_icon(
							gui_globals.hex_to_rgb(diff_markup.COLOR_DIFF_DEL)));

				} else if (event.getSource().equals(browser_color_note)) {
					diff_markup.COLOR_DIFF_NOTE = gui_colorpicker.
							dialog_response(parent, diff_markup.COLOR_DIFF_NOTE);
					browser_color_note.setIcon(new gui_color_icon(
							gui_globals.hex_to_rgb(diff_markup.COLOR_DIFF_NOTE)));

				} else if (event.getSource().equals(browser_color_reset)) {

					// Reset colors, then repaint icons accordingly
					diff_markup.reset_default_colors();
					browser_color_bg.setIcon(new gui_color_icon(
							gui_globals.hex_to_rgb(diff_markup.COLOR_DIFF_BG)));
					browser_color_text.setIcon(new gui_color_icon(
							gui_globals.hex_to_rgb(diff_markup.COLOR_DIFF_TEXT)));
					browser_color_words.setIcon(new gui_color_icon(
							gui_globals.hex_to_rgb(diff_markup.COLOR_DIFF_WORDS)));
					browser_color_cont.setIcon(new gui_color_icon(
							gui_globals.hex_to_rgb(diff_markup.COLOR_DIFF_CONT)));
					browser_color_add.setIcon(new gui_color_icon(
							gui_globals.hex_to_rgb(diff_markup.COLOR_DIFF_ADD)));
					browser_color_del.setIcon(new gui_color_icon(
							gui_globals.hex_to_rgb(diff_markup.COLOR_DIFF_DEL)));
					browser_color_note.setIcon(new gui_color_icon(
							gui_globals.hex_to_rgb(diff_markup.COLOR_DIFF_NOTE)));
				}

			} catch (Exception e) {
			}
		}
	}


	// ***** ACESSOR METHODS *****

	/**
	 * Return the point-size of the currently selected browser font.
	 *
	 * @return the point-size of the currently selected browser font
	 */
	public int get_browser_fontsize() {
		if (browser_font_10.isSelected()) return 10;
		else if (browser_font_12.isSelected()) return 12;
		else if (browser_font_14.isSelected()) return 14;
		else if (browser_font_16.isSelected()) return 16;
		else if (browser_font_18.isSelected()) return 18;
		else if (browser_font_20.isSelected()) return 20;
		else if (browser_font_22.isSelected()) return 22;
		else if (browser_font_24.isSelected()) return 24;
		else return (gui_globals.DEFAULT_BROWSER_FONT.getSize()); // unneeded
	}

	/**
	 * Return the font family of the currently selected browser font.
	 *
	 * @return the font family of the currently selected browser font
	 */
	public String get_browser_fontfam() {
		if (browser_font_serif.isSelected()) return Font.SERIF;
		else if (browser_font_sans.isSelected()) return Font.SANS_SERIF;
		else if (browser_font_mono.isSelected()) return Font.MONOSPACED;
		else return (gui_globals.DEFAULT_BROWSER_FONT.getFamily()); // unneeded
	}

	/**
	 * Set the hyperlink policy in the menu (and the entire GUI).
	 *
	 * @param enable TRUE if hyperlinks in diff-display should be enabled
	 *               (click-able). FASLE, otherwise.
	 */
	public void set_hyperlink_policy(boolean enable) {
		xlink_cb.setSelected(enable);
		parent.diff_browser.set_hyperlink_policy(enable);
	}

	/**
	 * Return whether the 'activate hyperlinks' checkbox is selected.
	 *
	 * @return whether the 'activate hyperlinks' checkbox is selected
	 */
	public boolean get_hyperlink_policy() {
		return (xlink_cb.isSelected());
	}

	/**
	 * Set the DTTR policy in the menu (i.e., 'warn if templating regular')
	 *
	 * @param enable TRUE if editors should be warned if trying to revert/
	 *               template/warn a regular user (whose definition is elsewehere).
	 */
	public void set_dttr_policy(boolean enable) {
		dttr_cb.setSelected(enable);
	}

	/**
	 * Return whether the 'warn if templating regular' checkbox is selected.
	 *
	 * @return whether the 'warn if templating regular' checkbox is selected
	 */
	public boolean get_dttr_policy() {
		return (dttr_cb.isSelected());
	}

	/**
	 * Set the AGF comment policy in the menu
	 *
	 * @param enable TRUE if STiki users will be given the opportunity to
	 *               send a message to editors reverted in an AGF fashion. Else, false.
	 */
	public void set_agf_comment_policy(boolean enable) {
		agf_comment_cb.setSelected(enable);
	}

	/**
	 * Return whether the 'AGF comment' checkbox is selected.
	 *
	 * @return whether the 'AGF comment' checkbox is selected
	 */
	public boolean get_agf_comment_policy() {
		return (agf_comment_cb.isSelected());
	}

	/**
	 * Set the AIV popup policy in the menu
	 *
	 * @param enable TRUE if STiki users will be popped an explicit
	 *               notification window upon posting to AIV. Else, false.
	 */
	public void set_aiv_popup_policy(boolean enable) {
		aiv_popup_cb.setSelected(enable);
	}

	/**
	 * Return whether the 'AIV popup' checkbox is selected.
	 *
	 * @return whether the 'AIV popup' checkbox is selected
	 */
	public boolean get_aiv_popup_policy() {
		return (aiv_popup_cb.isSelected());
	}


	// **************************** PRIVATE FIELDS ***************************

	/**
	 * Initialize the submenus/items beneath the "appearance" menu header.
	 */
	private void initialize_subitems() {

		// Intialize the sub-menu header for "browser font size"
		submenu_browser_font_size = new JMenu("Browser Font Size");
		submenu_browser_font_size.setFont(gui_globals.PLAIN_NORMAL_FONT);
		submenu_browser_font_size.setMnemonic(KeyEvent.VK_S);

		// Then add items to the "browser font size" sub-menu
		submenu_browser_font_size.add(browser_font_10 = create_rb_item(
				"10 point", KeyEvent.VK_1));
		submenu_browser_font_size.add(browser_font_12 = create_rb_item(
				"12 point", KeyEvent.VK_2));
		submenu_browser_font_size.add(browser_font_14 = create_rb_item(
				"14 point", KeyEvent.VK_4));
		submenu_browser_font_size.add(browser_font_16 = create_rb_item(
				"16 point", KeyEvent.VK_6));
		submenu_browser_font_size.add(browser_font_18 = create_rb_item(
				"18 point", KeyEvent.VK_8));
		submenu_browser_font_size.add(browser_font_20 = create_rb_item(
				"20 point", KeyEvent.VK_0));
		submenu_browser_font_size.add(browser_font_22 = create_rb_item(
				"22 point", KeyEvent.VK_P));
		submenu_browser_font_size.add(browser_font_24 = create_rb_item(
				"24 point", KeyEvent.VK_O));

		// Intialize the sub-menu for "font family" and its sub-tems
		submenu_browser_font_family = new JMenu("Browser Font Family");
		submenu_browser_font_family.setFont(gui_globals.PLAIN_NORMAL_FONT);
		submenu_browser_font_family.setMnemonic(KeyEvent.VK_F);
		submenu_browser_font_family.add(browser_font_serif = create_rb_item(
				"Serif", KeyEvent.VK_E));
		submenu_browser_font_family.add(browser_font_sans = create_rb_item(
				"Sans Serif", KeyEvent.VK_I));
		submenu_browser_font_family.add(browser_font_mono = create_rb_item(
				"Monospace", KeyEvent.VK_M));

		// Initialize the sub-menu header for "browser colors"
		submenu_browser_colors = new JMenu("Browser Colors (restart)");
		submenu_browser_colors.setFont(gui_globals.PLAIN_NORMAL_FONT);
		submenu_browser_colors.setMnemonic(KeyEvent.VK_C);

		// Add the individual color fields
		submenu_browser_colors.add(browser_color_bg = create_icon_item(
				"Background", new gui_color_icon(gui_globals.hex_to_rgb(
						diff_markup.COLOR_DIFF_BG)), KeyEvent.VK_B));
		submenu_browser_colors.add(browser_color_text = create_icon_item(
				"Plain text", new gui_color_icon(gui_globals.hex_to_rgb(
						diff_markup.COLOR_DIFF_TEXT)), KeyEvent.VK_L));
		submenu_browser_colors.add(browser_color_words = create_icon_item(
				"Changed text", new gui_color_icon(gui_globals.hex_to_rgb(
						diff_markup.COLOR_DIFF_WORDS)), KeyEvent.VK_C));
		submenu_browser_colors.add(browser_color_note = create_icon_item(
				"Note text", new gui_color_icon(gui_globals.hex_to_rgb(
						diff_markup.COLOR_DIFF_NOTE)), KeyEvent.VK_N));
		submenu_browser_colors.add(browser_color_cont = create_icon_item(
				"Context block BG", new gui_color_icon(gui_globals.hex_to_rgb(
						diff_markup.COLOR_DIFF_CONT)), KeyEvent.VK_N));
		submenu_browser_colors.add(browser_color_add = create_icon_item(
				"Added block BG", new gui_color_icon(gui_globals.hex_to_rgb(
						diff_markup.COLOR_DIFF_ADD)), KeyEvent.VK_D));
		submenu_browser_colors.add(browser_color_del = create_icon_item(
				"Removed block BG", new gui_color_icon(gui_globals.hex_to_rgb(
						diff_markup.COLOR_DIFF_DEL)), KeyEvent.VK_R));
		submenu_browser_colors.add(browser_color_reset = create_icon_item(
				"Reset defaults", null, KeyEvent.VK_T));

		xlink_cb = create_cb_item("Activate Ext-Links", KeyEvent.VK_X);
		dttr_cb = create_cb_item("Warn if reverting regular", KeyEvent.VK_W);
		agf_comment_cb = create_cb_item("Message AGF reverted users", KeyEvent.VK_A);
		aiv_popup_cb = create_cb_item("Explicit notify on AIV post", KeyEvent.VK_V);
	}

	/**
	 * Alter which of the 'browser-font' radio buttons is currently selected.
	 *
	 * @param font_size Integer point size of the button to be selected
	 */
	private void selected_browser_font_size(int font_size) {

		// Begin by un-setting all radio buttons
		browser_font_10.setSelected(false);
		browser_font_12.setSelected(false);
		browser_font_14.setSelected(false);
		browser_font_16.setSelected(false);
		browser_font_18.setSelected(false);
		browser_font_20.setSelected(false);
		browser_font_22.setSelected(false);
		browser_font_24.setSelected(false);

		switch (font_size) {
			case 10:
				browser_font_10.setSelected(true);
				break;
			case 12:
				browser_font_12.setSelected(true);
				break;
			case 14:
				browser_font_14.setSelected(true);
				break;
			case 16:
				browser_font_16.setSelected(true);
				break;
			case 18:
				browser_font_18.setSelected(true);
				break;
			case 20:
				browser_font_20.setSelected(true);
				break;
			case 22:
				browser_font_22.setSelected(true);
				break;
			case 24:
				browser_font_24.setSelected(true);
				break;
		} // Then "select" the appropriate one		

		// Pass the actual change off to the browser
		Font new_font = new Font(gui_diff_panel.browser_font.getFamily(),
				gui_diff_panel.browser_font.getStyle(), font_size);
		parent.diff_browser.change_browser_font(new_font);
	}

	/**
	 * Alter which of the 'font family' radio buttons is selected
	 *
	 * @param font_fam String label of family to be selected.
	 */
	private void selected_browser_font_fam(String font_fam) {

		// Unset everything
		browser_font_serif.setSelected(false);
		browser_font_sans.setSelected(false);
		browser_font_mono.setSelected(false);

		if (font_fam.equals(Font.SERIF))
			browser_font_serif.setSelected(true);
		else if (font_fam.equals(Font.SANS_SERIF))
			browser_font_sans.setSelected(true);
		else if (font_fam.equals(Font.MONOSPACED))
			browser_font_mono.setSelected(true);

		Font new_font = new Font(font_fam,
				gui_diff_panel.browser_font.getStyle(),
				gui_diff_panel.browser_font.getSize());
		parent.diff_browser.change_browser_font(new_font);
	}


	// ***** SIMPLIFICATIONS OF GUI_GLOBALS() FOR THIS CLASS

	/**
	 * Create a radio-button menu-item of the style used by STiki
	 *
	 * @param text     Text which should be displayed on the menu-item
	 * @param keyevent Key mnemonic to fire this item
	 * @return A radio-button menu-item, labeled as 'text', fired by 'keyevent'
	 */
	private JRadioButtonMenuItem create_rb_item(String text, int keyevent) {
		JRadioButtonMenuItem rb_item = new JRadioButtonMenuItem(text);
		rb_item.setMnemonic(keyevent);
		rb_item.setFont(gui_globals.PLAIN_NORMAL_FONT);
		rb_item.addActionListener(this);
		return (rb_item);
	}

	/**
	 * Create a checkbox menu-item of the style used by STiki
	 *
	 * @param text     Text which should be displayed on the menu-item
	 * @param keyevent Key mnemonic to fire this item
	 * @return A checkbox menu-item, labeled as 'text', fired by 'keyevent'
	 */
	private JCheckBoxMenuItem create_cb_item(String text, int keyevent) {
		JCheckBoxMenuItem cb_item = new JCheckBoxMenuItem(text);
		cb_item.setMnemonic(keyevent);
		cb_item.setFont(gui_globals.PLAIN_NORMAL_FONT);
		cb_item.addActionListener(this);
		return (cb_item);
	}

	/**
	 * Create a plain menu-item of the style used by STiki
	 *
	 * @param text     Text which should be displayed on the menu-item
	 * @param icon     Icon to be displayed adjacent to text
	 * @param keyevent Key mnemonic to fire this item
	 * @return A plain menu-item, labeled as 'text', fired by 'keyevent'
	 */
	private JMenuItem create_icon_item(String text, Icon icon, int keyevent) {
		JMenuItem item = new JMenuItem(text, icon);
		item.setMnemonic(keyevent);
		item.setFont(gui_globals.PLAIN_NORMAL_FONT);
		item.addActionListener(this);
		return (item);
	}

}
