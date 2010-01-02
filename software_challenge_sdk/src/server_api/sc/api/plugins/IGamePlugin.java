package sc.api.plugins;

import edu.cau.plugins.IPlugin;
import sc.api.plugins.host.IGamePluginHost;
import sc.shared.ScoreDefinition;

public interface IGamePlugin extends IPlugin<IGamePluginHost>
{
	/**
	 * Creates a new game of this type.
	 * 
	 * @return
	 */
	public IGameInstance createGame();

	/**
	 * 
	 * @return The maximum number of supported players which should be able to
	 *         join from the lobby.
	 */
	public int getMaximumPlayerCount();

	public ScoreDefinition getScoreDefinition();
}