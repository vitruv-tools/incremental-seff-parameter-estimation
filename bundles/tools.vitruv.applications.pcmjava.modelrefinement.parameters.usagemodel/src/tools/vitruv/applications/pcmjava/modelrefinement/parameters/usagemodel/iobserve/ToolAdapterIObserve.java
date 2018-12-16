package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.iobserve;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;

public class ToolAdapterIObserve {

	public static void run(String kiekerPath, String modelsPath) throws CoreException {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();

		ILaunchConfigurationType type = manager
				.getLaunchConfigurationType("org.eclipse.cdt.launch.applicationLaunchType");
		ILaunchConfiguration[] lcs = manager.getLaunchConfigurations(type);

		for (ILaunchConfiguration iLaunchConfiguration : lcs) {
			if (iLaunchConfiguration.getName().equals("Test PThread")) {
				ILaunchConfigurationWorkingCopy t = iLaunchConfiguration.getWorkingCopy();
				ILaunchConfiguration config = t.doSave();
				if (config != null) {
					// config.launch(ILaunchManager.RUN_MODE, null);
					DebugUITools.launch(config, ILaunchManager.DEBUG_MODE);
				}
			}
		}
	}

}
