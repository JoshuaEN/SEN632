package ojdev.common;

import ojdev.common.test.DebugMode;

/**
 * Store for static variables used by both the Client and Server.
 */
public final class SharedConstant {

	/**
	 * Default Connection Port
	 */
	public static final int DEFAULT_PORT = 1510;

	public static final DebugMode DEBUG_MODE = DebugMode.VERBOSE;
	
	public static final boolean DEBUG = true;
}