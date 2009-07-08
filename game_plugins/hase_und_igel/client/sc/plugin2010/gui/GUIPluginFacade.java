package sc.plugin2010.gui;

import java.awt.Image;
import java.io.IOException;

import javax.swing.JPanel;

import sc.guiplugin.interfaces.IGUIPluginFacade;
import sc.guiplugin.interfaces.IGamePreparation;
import sc.plugin2010.Client;
import sc.plugin2010.renderer.RenderFacade;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * @author ffi
 * 
 */
public class GUIPluginFacade implements IGUIPluginFacade
{
	/**
	 * Singleton instance
	 */
	private static volatile GUIPluginFacade	instance;

	private GUIPluginFacade()
	{ // Singleton
	}

	public static GUIPluginFacade getInstance()
	{
		if (null == instance)
		{
			synchronized (GUIPluginFacade.class)
			{
				if (null == instance)
				{
					instance = new GUIPluginFacade();
				}
			}
		}
		return instance;
	}

	@Override
	public void setRenderContext(JPanel panel, boolean threeDimensional)
	{
		RenderFacade.getInstance().createInitFrame(panel, threeDimensional);
	}

	@Override
	public Image getCurrentStateImage()
	{
		return RenderFacade.getInstance().getImage();

	}

	public String getPluginVersion()
	{
		return "0.1 alpha"; // TODO
	}

	public boolean connectToServer(final String ip, final int port)
	{
		return false; // TODO
	}

	public IGamePreparation prepareGame(final String ip, final int port)
			throws IOException
	{
		Client client = new Client("Hase und Igel", new XStream(), ip, port);
		client.setHandler(new GameHandler());
		return new GamePreparation(client);
	}
}
