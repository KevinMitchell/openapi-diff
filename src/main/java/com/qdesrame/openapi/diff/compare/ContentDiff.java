package com.qdesrame.openapi.diff.compare;

import com.qdesrame.openapi.diff.model.ChangedContent;
import com.qdesrame.openapi.diff.model.ChangedMediaType;
import com.qdesrame.openapi.diff.model.ChangedSchema;
import io.swagger.oas.models.Components;
import io.swagger.oas.models.media.Content;
import io.swagger.oas.models.media.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentDiff implements Comparable<Content> {

    private Components leftComponents;
    private Components rightComponents;

    private ContentDiff(Components left, Components right) {
        this.leftComponents = left;
        this.rightComponents = right;
    }

    public static ContentDiff fromComponents(Components left, Components right) {
        return new ContentDiff(left, right);
    }

    @Override
    public boolean compare(Content left, Content right) {
        return false;
    }

    public ChangedContent diff(Content left, Content right) {
        ChangedContent changedContent = new ChangedContent(left, right);

        MapKeyDiff<String, MediaType> mediaTypeDiff = MapKeyDiff.diff(left, right);
        changedContent.setIncreased(mediaTypeDiff.getIncreased());
        changedContent.setMissing(mediaTypeDiff.getMissing());
        List<String> sharedMediaTypes = mediaTypeDiff.getSharedKey();
        Map<String, ChangedMediaType> changedMediaTypes = new HashMap<>();
        for (String mediaTypeKey : sharedMediaTypes) {
            MediaType oldMediaType = left.get(mediaTypeKey);
            MediaType newMediaType = right.get(mediaTypeKey);
            ChangedSchema changedSchema = SchemaDiff.fromComponents(leftComponents, rightComponents)
                    .diff(oldMediaType.getSchema(), newMediaType.getSchema());
            ChangedMediaType changedMediaType = new ChangedMediaType(oldMediaType.getSchema(), newMediaType.getSchema());
            changedMediaType.setChangedSchema(changedSchema);
            if (changedMediaType.isDiff()) {
                changedMediaTypes.put(mediaTypeKey, changedMediaType);
            }
        }
        changedContent.setChanged(changedMediaTypes);
        return changedContent;
    }
}
