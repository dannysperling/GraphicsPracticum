package cs4620.common.event;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import cs4620.common.SceneRaster;

public class SceneEventQueue {
	public final SceneRaster scene;
	public final LinkedBlockingQueue<SceneEvent> queue = new LinkedBlockingQueue<>();
	
	public SceneEventQueue(SceneRaster s) {
		scene = s;
	}

	public void getEvents(ArrayList<SceneEvent> a) {
		queue.drainTo(a);
	}
	public void addEvent(SceneEvent e) {
		queue.offer(e);
	}
}
