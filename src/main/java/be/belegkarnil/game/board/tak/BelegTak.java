/*
 *  Copyright 2025 Belegkarnil
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 *  associated documentation files (the “Software”), to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
 *  so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 *  OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package be.belegkarnil.game.board.tak;

import be.belegkarnil.game.board.tak.gui.MainFrame;
import be.belegkarnil.game.board.tak.strategy.Strategy;
import be.belegkarnil.game.board.tak.strategy.StrategyAdapter;

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Enumeration;
import java.util.Comparator;
import java.util.Set;

/**
 * This class is the main class that run the game with GUI.
 *
 * @author Belegkarnil
 */
public class BelegTak{
	private static final Set<Class<? extends Strategy>> strategies = new HashSet<Class<? extends Strategy>>();

	public static void loadStrategies() throws IOException, ClassNotFoundException{
		loadStrategies(BelegTak.class.getPackageName());
	}

	public static void loadStrategies(final String packageName) throws IOException, ClassNotFoundException{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Enumeration<URL> resources = classLoader.getResources(packageName.replace(".", "/"));
		java.util.List<File> dirs = new ArrayList<File>();
		while(resources.hasMoreElements()){
			URL resource = resources.nextElement();
			final String path = resource.getFile();
			dirs.add(new File(path));
			if(path.contains("%20")){// Compatibilty for Java impl, that URI Encode
				dirs.add(new File(path.replace("%20"," ")));
			}
		}
		ArrayList<Class> classes = new ArrayList<Class>();
		for(File directory : dirs){
			classes.addAll(findClasses(directory, packageName));
		}
		for(Class<?> klass : classes){
			if(isStrategy(klass) && hasDefaultConstructor(klass)){
				strategies.add((Class<Strategy>) klass);
			}
		}
	}

	public static boolean isStrategy(Class klass){
		Class<?>[] interfaces = klass.getInterfaces();
		for(Class<?> inter : interfaces){
			if(inter.getName().equals(Strategy.class.getName())) return true;
		}
		Class<?> superclass = klass.getSuperclass();
		if(superclass != null && superclass.getName().equals(StrategyAdapter.class.getName())) return true;
		return false;
	}

	public static java.util.List<Constructor<Strategy>> constructorOnlyWith(Class<Strategy> klass, java.util.List<Class> classes){
		Constructor<Strategy>[] constructors = (Constructor<Strategy>[]) klass.getConstructors();
		java.util.List<Constructor<Strategy>> results = new LinkedList<Constructor<Strategy>>();
		for(Constructor<Strategy> constructor : constructors){
			Class<?>[] types = constructor.getParameterTypes();
			boolean respect = true;
			for(Class<?> type : types){
				if(!classes.contains(type))
					respect = false;
			}
			if(respect) results.add(constructor);
		}
		// More complex first
		results.sort(new Comparator<Constructor<Strategy>>(){
			@Override
			public int compare(Constructor<Strategy> a, Constructor<Strategy> b){
				return -Integer.compare(a.getParameterCount(), b.getParameterCount());
			}
		});
		return results;
	}

	public static boolean hasDefaultConstructor(Class klass){
		try{
			Constructor<Strategy> constructor = klass.getConstructor();
			constructor.newInstance();
		}catch(NoSuchMethodException e){
			return false;
		}catch(InvocationTargetException e){
			return false;
		}catch(InstantiationException e){
			return false;
		}catch(IllegalAccessException e){
			return false;
		}
		return true;
	}

	private static java.util.List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException{
		java.util.List<Class> classes = new ArrayList<Class>();
		if(!directory.exists()){
			return classes;
		}
		File[] files = directory.listFiles();
		for(File file : files){
			if(file.isDirectory()){
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			}else if(file.getName().endsWith(".class")){
				classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

	public static Class<? extends Strategy>[] listStrategies(){
		return (Class<? extends Strategy>[]) strategies.toArray(new Class[strategies.size()]);
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException{
		loadStrategies();
		loadStrategies("be.heh");
		Window window = new MainFrame();
		window.pack();
		window.setLocationRelativeTo(window.getParent());
		window.setVisible(true);
	}
}