package sc.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import sc.protocol.IControllableGame;
import sc.protocol.clients.ObservingClient;

public class ReplayBuilder
{
	private static final Logger	logger	= LoggerFactory
												.getLogger(ReplayBuilder.class);

	public static void saveReplay(IControllableGame game, String pathname)
			throws IOException
	{
		File file = new File(pathname);

		if (!file.isDirectory())
		{
			saveReplay(game, new FileOutputStream(file));
		}
		else
		{
			logger.warn("Cannot save replay as directory");
		}
	}

	public static void saveReplay(IControllableGame game, File file)
			throws IOException
	{
		saveReplay(game, new FileOutputStream(file));
	}

	public static void saveReplay(IControllableGame game, OutputStream out)
			throws IOException
	{
		if (game instanceof ObservingClient)
		{
			ObservingClient client = (ObservingClient) game;
			ObjectOutputStream objectOut = new XStream()
					.createObjectOutputStream(out);

			for (Object state : client.getHistory())
			{
				objectOut.writeObject(state);
			}

			objectOut.flush();
		}
		else
		{
			logger.warn("{} not supported", game.getClass());
		}
	}
}