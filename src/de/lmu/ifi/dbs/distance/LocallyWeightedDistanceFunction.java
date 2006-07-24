package de.lmu.ifi.dbs.distance;

import java.util.List;

import de.lmu.ifi.dbs.data.RealVector;
import de.lmu.ifi.dbs.database.AssociationID;
import de.lmu.ifi.dbs.database.Database;
import de.lmu.ifi.dbs.database.DatabaseEvent;
import de.lmu.ifi.dbs.database.DatabaseListener;
import de.lmu.ifi.dbs.index.spatial.MBR;
import de.lmu.ifi.dbs.index.spatial.SpatialDistanceFunction;
import de.lmu.ifi.dbs.math.linearalgebra.Matrix;
import de.lmu.ifi.dbs.preprocessing.HiCOPreprocessor;
import de.lmu.ifi.dbs.preprocessing.KnnQueryBasedHiCOPreprocessor;
import de.lmu.ifi.dbs.preprocessing.Preprocessor;
import de.lmu.ifi.dbs.properties.Properties;
import de.lmu.ifi.dbs.utilities.UnableToComplyException;
import de.lmu.ifi.dbs.utilities.Util;
import de.lmu.ifi.dbs.utilities.optionhandling.AttributeSettings;
import de.lmu.ifi.dbs.utilities.optionhandling.Flag;
import de.lmu.ifi.dbs.utilities.optionhandling.Parameter;
import de.lmu.ifi.dbs.utilities.optionhandling.ParameterException;
import de.lmu.ifi.dbs.utilities.optionhandling.WrongParameterValueException;

/**
 * Provides a locally weighted distance function.
 * Computes the quadratic form distance between two vectors P and Q as follows:
 * result = max{dist<sub>P</sub>(P,Q), dist<sub>Q</sub>(Q,P)}
 * where dist<sub>X</sub>(X,Y) = (X-Y)*<b>M<sub>X</sub></b>*(X-Y)<b><sup>T</sup></b>
 * and <b>M<sub>X</sub></b> is the weight matrix of vector X.
 *
 * @author Arthur Zimek (<a
 *         href="mailto:zimek@dbs.ifi.lmu.de">zimek@dbs.ifi.lmu.de</a>)
 */
public class LocallyWeightedDistanceFunction extends DoubleDistanceFunction<RealVector>
implements SpatialDistanceFunction<RealVector, DoubleDistance>, DatabaseListener {

  /**
   * The default preprocessor class name.
   */
  public static final String DEFAULT_PREPROCESSOR_CLASS = KnnQueryBasedHiCOPreprocessor.class.getName();

  /**
   * Parameter for preprocessor.
   */
  public static final String PREPROCESSOR_CLASS_P = "preprocessor";

  /**
   * Description for parameter preprocessor.
   */
  public static final String PREPROCESSOR_CLASS_D = "the preprocessor to determine the correlation dimensions of the objects " +
                                                    Properties.KDD_FRAMEWORK_PROPERTIES.restrictionString(HiCOPreprocessor.class) +
                                                    ". Default: " + DEFAULT_PREPROCESSOR_CLASS;

  /**
   * Flag for omission of preprocessing.
   */
  public static final String OMIT_PREPROCESSING_F = "omitPreprocessing";

  /**
   * Description for flag for force of preprocessing.
   */
  public static final String OMIT_PREPROCESSING_D = "flag to omit (a new) preprocessing if for each object a matrix already has been associated.";

  /**
   * Whether preprocessing is omitted.
   */
  private boolean omit;

  /**
   * The preprocessor to determine the correlation dimensions of the objects.
   */
  private Preprocessor preprocessor;

  /**
   * Indicates if the verbose flag is set for preprocessing..
   */
  private boolean verbose;

  /**
   * Indicates if the time flag is set for preprocessing.
   */
  boolean time;

  /**
   * Provides a locally weighted distance function.
   */
  public LocallyWeightedDistanceFunction() {
    super();

    optionHandler.put(PREPROCESSOR_CLASS_P, new Parameter(PREPROCESSOR_CLASS_P,PREPROCESSOR_CLASS_D,Parameter.Types.CLASS));
    optionHandler.put(OMIT_PREPROCESSING_F, new Flag(OMIT_PREPROCESSING_F,OMIT_PREPROCESSING_D));
  }

  /**
   * Computes the distance between two given real vectors according to this
   * distance function.
   *
   * @param o1 first RealVector
   * @param o2 second RealVector
   * @return the distance between two given real vectors according to this
   *         distance function
   */
  public DoubleDistance distance(RealVector o1, RealVector o2) {
    Matrix m1 = (Matrix) getDatabase().getAssociation(AssociationID.LOCALLY_WEIGHTED_MATRIX, o1.getID());
    Matrix m2 = (Matrix) getDatabase().getAssociation(AssociationID.LOCALLY_WEIGHTED_MATRIX, o2.getID());

    if (m1 == null || m2 == null) {
      return new DoubleDistance(Double.POSITIVE_INFINITY);
    }

    //noinspection unchecked
    Matrix rv1Mrv2 = o1.plus(o2.negativeVector()).getColumnVector();
    //noinspection unchecked
    Matrix rv2Mrv1 = o2.plus(o1.negativeVector()).getColumnVector();

    double dist1 = rv1Mrv2.transpose().times(m1).times(rv1Mrv2).get(0, 0);
    double dist2 = rv2Mrv1.transpose().times(m2).times(rv2Mrv1).get(0, 0);

    return new DoubleDistance(Math.max(Math.sqrt(dist1), Math.sqrt(dist2)));
  }

  /**
   * @see DistanceFunction#setDatabase(de.lmu.ifi.dbs.database.Database, boolean, boolean)
   */
  public void setDatabase(Database<RealVector> database, boolean verbose, boolean time) {
    super.setDatabase(database, verbose, time);
    this.verbose = verbose;
    this.time = time;
    database.addDatabaseListener(this);

    if (! omit || !database.isSet(AssociationID.LOCALLY_WEIGHTED_MATRIX)) {
      preprocessor.run(getDatabase(), verbose, time);
    }
  }

  /**
   * @see de.lmu.ifi.dbs.utilities.optionhandling.Parameterizable#description()
   */
  public String description() {
    StringBuffer description = new StringBuffer();
    description.append(optionHandler.usage("Locally weighted distance function. Pattern for defining a range: \"" + requiredInputPattern() + "\".", false));
    description.append('\n');
    description.append("Preprocessors available within this framework for distance function ");
    description.append(this.getClass().getName());
    description.append(":");
    description.append('\n' + Properties.KDD_FRAMEWORK_PROPERTIES.restrictionString(preprocessor.getClass()));
    description.append('\n');
    return description.toString();
  }

  /**
   * @see de.lmu.ifi.dbs.utilities.optionhandling.Parameterizable#setParameters(String[])
   */
  public String[] setParameters(String[] args) throws ParameterException {
    String[] remainingParameters = super.setParameters(args);

    // preprocessor
    if (optionHandler.isSet(PREPROCESSOR_CLASS_P)) {
      try {
        preprocessor = Util.instantiate(Preprocessor.class, optionHandler.getOptionValue(PREPROCESSOR_CLASS_P));
      }
      catch (UnableToComplyException e) {
        e.printStackTrace();
        throw new WrongParameterValueException(PREPROCESSOR_CLASS_P, optionHandler.getOptionValue(PREPROCESSOR_CLASS_P), PREPROCESSOR_CLASS_D, e);  //To change body of catch statement use File | Settings | File Templates.
      }
    }
    else {
      try {
        preprocessor = Util.instantiate(Preprocessor.class, DEFAULT_PREPROCESSOR_CLASS);
      }
      catch (UnableToComplyException e) {
        throw new WrongParameterValueException(PREPROCESSOR_CLASS_P, DEFAULT_PREPROCESSOR_CLASS, PREPROCESSOR_CLASS_D, e);  //To change body of catch statement use File | Settings | File Templates.
      }
    }

    // force flag
    omit = optionHandler.isSet(OMIT_PREPROCESSING_F);

    remainingParameters = preprocessor.setParameters(remainingParameters);
    setParameters(args, remainingParameters);
    return remainingParameters;
  }

  /**
   * @see de.lmu.ifi.dbs.utilities.optionhandling.Parameterizable#getAttributeSettings()
   */
  public List<AttributeSettings> getAttributeSettings() {
    List<AttributeSettings> result = super.getAttributeSettings();

    AttributeSettings settings = result.get(0);
    settings.addSetting(PREPROCESSOR_CLASS_P, preprocessor.getClass().getName());
    settings.addSetting(OMIT_PREPROCESSING_F, Boolean.toString(omit));

    result.addAll(preprocessor.getAttributeSettings());

    return result;
  }

  /**
   * Computes the minimum distance between the given MBR and the RealVector
   * object according to this distance function.
   *
   * @param mbr the MBR object
   * @param o   the FeatureVector object
   * @return the minimum distance between the given MBR and the SpatialData
   *         object according to this distance function
   * @see de.lmu.ifi.dbs.index.spatial.SpatialDistanceFunction#minDist(de.lmu.ifi.dbs.index.spatial.MBR, de.lmu.ifi.dbs.data.NumberVector)
   */
  public DoubleDistance minDist(MBR mbr, RealVector o) {
    if (mbr.getDimensionality() != o.getDimensionality()) {
      throw new IllegalArgumentException("Different dimensionality of objects\n  first argument: " + mbr.toString() + "\n  second argument: " + o.toString());
    }

    double[] r = new double[o.getDimensionality()];
    for (int d = 1; d <= o.getDimensionality(); d++) {
      double value = o.getValue(d).doubleValue();
      if (value < mbr.getMin(d))
        r[d - 1] = mbr.getMin(d);
      else if (value > mbr.getMax(d))
        r[d - 1] = mbr.getMax(d);
      else
        r[d - 1] = value;
    }

    RealVector mbrVector = o.newInstance(r);
    Matrix m = (Matrix) getDatabase().getAssociation(AssociationID.LOCALLY_WEIGHTED_MATRIX, o.getID());
    //noinspection unchecked
    Matrix rv1Mrv2 = o.plus(mbrVector.negativeVector()).getColumnVector();
    double dist = rv1Mrv2.transpose().times(m).times(rv1Mrv2).get(0, 0);

    return new DoubleDistance(Math.sqrt(dist));
  }

  /**
   * Computes the minimum distance between the given MBR and the NumberVector object
   * with the given id according to this distance function.
   *
   * @param mbr the MBR object
   * @param id  the id of the NumberVector object
   * @return the minimum distance between the given MBR and the SpatialData object
   *         according to this distance function
   */
  public DoubleDistance minDist(MBR mbr, Integer id) {
    return minDist(mbr, getDatabase().get(id));
  }

  /**
   * Computes the distance between the two given MBRs according to this
   * distance function.
   *
   * @param mbr1 the first MBR object
   * @param mbr2 the second MBR object
   * @return the distance between the two given MBRs according to this
   *         distance function
   * @see de.lmu.ifi.dbs.index.spatial.SpatialDistanceFunction#distance(MBR, MBR)
   */
  public DoubleDistance distance(MBR mbr1, MBR mbr2) {
    if (mbr1.getDimensionality() != mbr2.getDimensionality()) {
      throw new IllegalArgumentException("Different dimensionality of objects\n  first argument: " + mbr1.toString() + "\n  second argument: " + mbr2.toString());
    }

    double sqrDist = 0;
    for (int d = 1; d <= mbr1.getDimensionality(); d++) {
      double m1, m2;
      if (mbr1.getMax(d) < mbr2.getMin(d)) {
        m1 = mbr1.getMax(d);
        m2 = mbr2.getMin(d);
      }
      else if (mbr1.getMin(d) > mbr2.getMax(d)) {
        m1 = mbr1.getMin(d);
        m2 = mbr2.getMax(d);
      }
      else { // The mbrs intersect!
        m1 = 0;
        m2 = 0;
      }
      double manhattanI = m1 - m2;
      sqrDist += manhattanI * manhattanI;
    }
    return new DoubleDistance(Math.sqrt(sqrDist));
  }

  /**
   * Computes the distance between the centroids of the two given MBRs
   * according to this distance function.
   *
   * @param mbr1 the first MBR object
   * @param mbr2 the second MBR object
   * @return the distance between the centroids of the two given MBRs
   *         according to this distance function
   * @see de.lmu.ifi.dbs.index.spatial.SpatialDistanceFunction#centerDistance(MBR, MBR)
   */
  public DoubleDistance centerDistance(MBR mbr1, MBR mbr2) {
    if (mbr1.getDimensionality() != mbr2.getDimensionality()) {
      throw new IllegalArgumentException("Different dimensionality of objects\n  first argument: " + mbr1.toString() + "\n  second argument: " + mbr2.toString());
    }

    double sqrDist = 0;
    for (int d = 1; d <= mbr1.getDimensionality(); d++) {
      double c1 = (mbr1.getMin(d) + mbr1.getMax(d)) / 2;
      double c2 = (mbr2.getMin(d) + mbr2.getMax(d)) / 2;

      double manhattanI = c1 - c2;
      sqrDist += manhattanI * manhattanI;
    }
    return new DoubleDistance(Math.sqrt(sqrDist));
  }

  /**
   * Invoked after objects of the database have been updated in some way.
   * Use <code>e.getObjects()</code> to get the updated database objects.
   */
  public void objectsChanged(DatabaseEvent e) {
    if (! omit) {
      preprocessor.run(getDatabase(), verbose, time);
    }
  }

  /**
   * Invoked after an object has been inserted into the database.
   * Use <code>e.getObjects()</code> to get the newly inserted database objects.
   */
  public void objectsInserted(DatabaseEvent e) {
    if (! omit) {
      preprocessor.run(getDatabase(), verbose, time);
    }
  }

  /**
   * Invoked after an object has been deleted from the database.
   * Use <code>e.getObjects()</code> to get the inserted database objects.
   */
  public void objectsRemoved(DatabaseEvent e) {
    if (! omit) {
      preprocessor.run(getDatabase(), verbose, time);
    }
  }

}
