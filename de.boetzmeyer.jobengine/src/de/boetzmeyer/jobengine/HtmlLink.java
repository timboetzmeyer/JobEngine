package de.boetzmeyer.jobengine;

import java.util.ArrayList;
import java.util.List;

import de.boetzmeyer.jobengine.system.Strings;
import de.boetzmeyer.jobstore.jobstore.IRecordable;


final class HtmlLink {
	private static final long NO_KEY = 0L;

	private final long key;
	private final String displayName;
	
	public static HtmlLink createLink(final IRecordable inEntity) {
		if (inEntity != null) {
			return new HtmlLink(inEntity.getPrimaryKey(), inEntity.toString());
		}
		return new HtmlLink(NO_KEY, Strings.EMPTY);
	}

	public static List<HtmlLink> createLinks(final IRecordable ... inEntities) {
		final List<HtmlLink> links = new ArrayList<HtmlLink>();
		if (inEntities != null) {
			for (int i = 0; i < inEntities.length; i++) {
				if (inEntities[i] != null) {
					links.add(new HtmlLink(inEntities[i].getPrimaryKey(), inEntities[i].toString()));
				}
			}
		}
		return links;
	}
	
	public HtmlLink(final long inKey, final String inDisplayName) {
		if (inKey <= 0L) {
			throw new IllegalArgumentException(String.format("Key [%s] is invalid", Long.toString(inKey)));
		}
		if (Strings.isEmpty(inDisplayName)) {
			throw new IllegalArgumentException("DisplayName is empty");
		}
		key = inKey;
		displayName = inDisplayName;
	}

	@Override
	public String toString() {
		if (key != NO_KEY) {
			return String.format("<A href=\"%s\">%s</A>", Long.toString(key), displayName);
		}
		return displayName;
	}
}
