package sc.gui;

import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

import sc.IGUIApplication;
import sc.IPresentationFacade;
import sc.common.CouldNotFindAnyLanguageFile;
import sc.common.IConfiguration;
import sc.gui.dialogs.ContextDisplay;
import sc.logic.ILogicFacade;

/**
 * The Software Challenge's implementation of {@link IPresentationFacade}.
 * 
 * @author chw
 * @since SC'09
 */
public class PresentationFacade implements IPresentationFacade {

	/**
	 * The menu bar
	 */
	private SCMenuBar menuBar;
	/**
	 * The context display where to let render a game
	 */
	private ContextDisplay contextDisplay;
	/**
	 * The status bar
	 */
	private StatusBar statusBar;
	/**
	 * For multi-language support
	 */
	private ResourceBundle languageData;
	/**
	 * The logic facade
	 */
	private ILogicFacade logic;
	/**
	 * Contains the current configuration data
	 */
	private IConfiguration config;
	/**
	 * The main frame
	 */
	private JFrame frame;

	/**
	 * Singleton instance
	 */
	private static volatile PresentationFacade instance;

	private PresentationFacade() { // Singleton
	}

	public static PresentationFacade getInstance() {
		if (null == instance) {
			synchronized (PresentationFacade.class) {
				if (null == instance) {
					instance = new PresentationFacade();
				}
			}
		}
		return instance;
	}

	/**
	 * Initializes all internal attributes. Should only be called once at
	 * creation.
	 * 
	 * @param frame
	 * @param root
	 * @param logic
	 */
	public static PresentationFacade init(final JFrame frame, final IGUIApplication root,
			final ILogicFacade logic) {

		// create instance
		getInstance();

		// load configuration
		instance.config = logic.loadConfiguration();
		// load language data
		try {
			instance.languageData = logic.loadLanguageData(instance.config);
		} catch (CouldNotFindAnyLanguageFile e) {
			JOptionPane.showMessageDialog(frame, "Could not load any language file.",
					"Missing language file", JOptionPane.ERROR_MESSAGE);
			root.closeGUI();
		}
		
		instance.frame = frame;
		instance.logic = logic;
		instance.contextDisplay = new ContextDisplay();
		instance.menuBar = new SCMenuBar(root);
		instance.statusBar = new StatusBar();

		return instance;
	}

	@Override
	public JComponent getContextDisplay() {
		return contextDisplay;
	}

	@Override
	public JMenuBar getMenuBar() {
		return menuBar;
	}

	@Override
	public String getClientIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JComponent getStatusBar() {
		return statusBar;
	}

	@Override
	public void shutdown() {
		logic.saveConfiguration(config);
	}

	public ResourceBundle getLanguageData() {
		return languageData;
	}

	public ILogicFacade getLogicFacade() {
		return logic;
	}

	public JFrame getFrame() {
		return frame;
	}

}