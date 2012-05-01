package net.minecraft.src.mystlinkingbook;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an abstract path to a resource (a file, a folder, even if embedded in the mod's zip file).<br>
 * <br>
 * A path can be a root path or the child of a parent path. The resulting {@code toString()} is then the concatenation of the parents and the current path.<br>
 * <br>
 * You can use a dynamic path when the location of the resource may change.<br>
 * You can also disable a path when it should not be used to load a resource.
 * 
 * @author jlgrall
 * @since 0.8b (as a nested class of RessourcesManager, was named PathEnd)
 */
public class ResourcePath {
	
	public ResourcePath parent = null; // Considered final
	
	protected String path = ""; // Never null !
	
	protected boolean inPackage = false; // Considered final
	
	protected boolean isDynamic = false; // Can be changed at any time
	
	protected List<Kind> kinds = null;
	
	public ResourcePath(ResourcePath parent, String path) {
		this.parent = parent;
		this.path = path;
	}
	
	public ResourcePath(String path) {
		this.path = path;
	}
	
	public ResourcePath addkind(Kind kind) {
		if (kinds == null) {
			kinds = new ArrayList<Kind>(2);
		}
		kinds.add(kind);
		return this;
	}
	
	public boolean haskind(Kind kind) {
		if (kinds != null) {
			if (kinds.contains(kind)) return true;
		}
		return parent == null ? false : parent.haskind(kind);
	}
	
	/**
	 * This path is relative to the mod's zip file.
	 */
	public ResourcePath setInPackage() {
		inPackage = true;
		return this;
	}
	
	/**
	 * This path's exact location may change.
	 */
	public ResourcePath setDynamic() {
		isDynamic = true;
		return this;
	}
	
	public void setPath(String path) {
		if (isDynamic) {
			this.path = path;
		}
		else throw new IllegalStateException("This path is not dynamic: " + toString());
	}
	
	/**
	 * Is this path relative to the mod's zip file ?
	 */
	public boolean isInPackage() {
		return parent == null ? inPackage : parent.isInPackage();
	}
	
	public boolean isDynamic() {
		return isDynamic ? true : parent == null ? false : parent.isDynamic();
	}
	
	public boolean isDisabled() {
		if (kinds != null) {
			for (Kind kind : kinds) {
				if (kind.isDisabled()) return true;
			}
		}
		return parent == null ? false : parent.isDisabled();
	}
	
	/**
	 * Performs a file system call to check whether a file exists for this path.
	 */
	public boolean exists() {
		if (isDisabled()) return false;
		if (isInPackage()) return Mod_MystLinkingBook.class.getResource(toString()) != null;
		else return new File(toString()).exists();
	}
	
	public boolean isParentOf(ResourcePath path) {
		ResourcePath current = this;
		do {
			if (current.equals(path)) return true;
			current = current.parent;
		} while (current.parent != null);
		return false;
	}
	
	// For performance optimizations:
	public StringBuilder toString(StringBuilder builder) {
		if (parent != null) {
			parent.toString(builder);
		}
		builder.append(path);
		return builder;
	}
	
	@Override
	public String toString() {
		return parent == null ? path : toString(new StringBuilder()).toString();
	}
	
	/**
	 * Returns true if the two paths have the same location.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj != null && obj instanceof ResourcePath) {
			ResourcePath other = (ResourcePath)obj;
			return isInPackage() == other.isInPackage() && toString().equals(other.toString());
		}
		else return false;
	}
	
	/**
	 * Return a copy of the concatenation of this path (ie. the returned ResourcePath has no parent).<br>
	 * Useful to keep a freezed copy of a dynamic path.<br>
	 * <br>
	 * Note: the kinds of this ResourcePath (and his parents) are not copied to the new object.
	 */
	public ResourcePath copyFlatten() {
		ResourcePath path = new ResourcePath(toString());
		path.inPackage = isInPackage();
		// Does not copy the kinds.
		return path;
	}
	
	public static class Kind {
		protected String name = ""; // Never null !
		protected boolean disabled = false;
		
		public Kind(String name) {
			this.name = name;
		}
		
		public Kind(String name, boolean disabled) {
			this.name = name;
			this.disabled = disabled;
		}
		
		public boolean isDisabled() {
			return disabled;
		}
		
		/**
		 * Set the disabled state.
		 */
		public void setDisabled(boolean disabled) {
			this.disabled = disabled;
		}
		
	}
}