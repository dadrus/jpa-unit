package eu.drus.jpa.unit.mongodb.operation;

import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.model.Collation;
import com.mongodb.client.model.CollationAlternate;
import com.mongodb.client.model.CollationCaseFirst;
import com.mongodb.client.model.CollationMaxVariable;
import com.mongodb.client.model.CollationStrength;
import com.mongodb.client.model.IndexOptions;

public class IndexOptionsUtils {

    private IndexOptionsUtils() {}

    public static IndexOptions toIndexOptions(final Document options) {
        final IndexOptions indexOptions = new IndexOptions();

        applyIfTrue(options.containsKey("background"), () -> indexOptions.background(options.getBoolean("background")));
        applyIfTrue(options.containsKey("bits"), () -> indexOptions.bits(options.getInteger("bits")));
        applyIfTrue(options.containsKey("bucketSize"), () -> indexOptions.bucketSize(options.getDouble("bucketSize")));
        applyIfTrue(options.containsKey("collation"), () -> {
            final Document collationData = options.get("collation", Document.class);
            final Collation.Builder builder = Collation.builder();
            applyIfTrue(collationData.containsKey("backwards"), () -> builder.backwards(collationData.getBoolean("backwards")));
            applyIfTrue(collationData.containsKey("caseLevel"), () -> builder.caseLevel(collationData.getBoolean("caseLevel")));
            applyIfTrue(collationData.containsKey("alternate"),
                    () -> builder.collationAlternate(CollationAlternate.fromString(collationData.getString("alternate"))));
            applyIfTrue(collationData.containsKey("caseFirst"),
                    () -> builder.collationCaseFirst(CollationCaseFirst.fromString(collationData.getString("caseFirst"))));
            applyIfTrue(collationData.containsKey("maxVariable"),
                    () -> builder.collationMaxVariable(CollationMaxVariable.fromString(collationData.getString("maxVariable"))));
            applyIfTrue(collationData.containsKey("strength"),
                    () -> builder.collationStrength(CollationStrength.fromInt(collationData.getInteger("strength"))));
            applyIfTrue(collationData.containsKey("locale"), () -> builder.locale(collationData.getString("locale")));
            applyIfTrue(collationData.containsKey("normalization"), () -> builder.normalization(collationData.getBoolean("normalization")));
            applyIfTrue(collationData.containsKey("numericOrdering"),
                    () -> builder.numericOrdering(collationData.getBoolean("numericOrdering")));
            indexOptions.collation(builder.build());
        });
        applyIfTrue(options.containsKey("default_language"), () -> indexOptions.defaultLanguage(options.getString("default_language")));
        applyIfTrue(options.containsKey("expireAfterSeconds"),
                () -> indexOptions.expireAfter(options.getLong("expireAfterSeconds"), TimeUnit.SECONDS));
        applyIfTrue(options.containsKey("language_override"), () -> indexOptions.languageOverride(options.getString("language_override")));
        applyIfTrue(options.containsKey("max"), () -> indexOptions.max(options.getDouble("max")));
        applyIfTrue(options.containsKey("min"), () -> indexOptions.min(options.getDouble("min")));
        applyIfTrue(options.containsKey("name"), () -> indexOptions.name(options.getString("name")));
        applyIfTrue(options.containsKey("partialFilterExpression"),
                () -> indexOptions.partialFilterExpression(options.get("partialFilterExpression", Bson.class)));
        applyIfTrue(options.containsKey("sparse"), () -> indexOptions.sparse(options.getBoolean("sparse")));
        applyIfTrue(options.containsKey("sphereVersion"), () -> indexOptions.sphereVersion(options.getInteger("sphereVersion")));
        applyIfTrue(options.containsKey("storageEngine"), () -> indexOptions.storageEngine(options.get("storageEngine", Bson.class)));
        applyIfTrue(options.containsKey("textVersion"), () -> indexOptions.textVersion(options.getInteger("textVersion")));
        applyIfTrue(options.containsKey("unique"), () -> indexOptions.unique(options.getBoolean("unique")));
        applyIfTrue(options.containsKey("version"), () -> indexOptions.version(options.getInteger("version")));
        applyIfTrue(options.containsKey("weights"), () -> indexOptions.weights(options.get("weights", Bson.class)));

        return indexOptions;
    }

    private static void applyIfTrue(final boolean shouldApply, final Runnable run) {
        if (shouldApply) {
            run.run();
        }
    }
}
