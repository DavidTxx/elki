package de.lmu.ifi.dbs.elki.math;

import java.math.BigInteger;
import java.util.Random;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Matrix;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * A collection of math related utility functions.
 */
public final class MathUtil {
  /**
   * Two times Pi.
   */
  public static final double TWOPI = 2 * Math.PI;

  /**
   * Squre root of two times Pi.
   */
  public static final double SQRTTWOPI = Math.sqrt(TWOPI);

  /**
   * Square root of 2.
   */
  public static final double SQRT2 = Math.sqrt(2);

  /**
   * Square root of 0.5 == 1 / Sqrt(2)
   */
  public static final double SQRTHALF = Math.sqrt(.5);

  /**
   * Precomputed value of 1 / sqrt(pi)
   */
  public static final double ONE_BY_SQRTPI = 1 / Math.sqrt(Math.PI);

  /**
   * LANCZOS-Coefficients for Gamma approximation.
   * 
   * These have slightly higher precision than those in "Numerical Recipes".
   * They probably come from
   * 
   * Paul Godfrey: http://my.fit.edu/~gabdo/gamma.txt
   */
  static final double[] LANCZOS = { 0.99999999999999709182, 57.156235665862923517, -59.597960355475491248, 14.136097974741747174, -0.49191381609762019978, .33994649984811888699e-4, .46523628927048575665e-4, -.98374475304879564677e-4, .15808870322491248884e-3, -.21026444172410488319e-3, .21743961811521264320e-3, -.16431810653676389022e-3, .84418223983852743293e-4, -.26190838401581408670e-4, .36899182659531622704e-5, };

  /**
   * Numerical precision to use
   */
  static final double NUM_PRECISION = 1E-15;

  /**
   * Coefficients for erf approximation.
   *
   * Loosely based on http://www.netlib.org/specfun/erf
   */
  static final double erfapp_a[] = { 1.85777706184603153e-1, 3.16112374387056560e+0, 1.13864154151050156E+2, 3.77485237685302021e+2, 3.20937758913846947e+3 };

  /**
   * Coefficients for erf approximation.
   *
   * Loosely based on http://www.netlib.org/specfun/erf
   */
  static final double erfapp_b[] = { 1.00000000000000000e00, 2.36012909523441209e01, 2.44024637934444173e02, 1.28261652607737228e03, 2.84423683343917062e03 };

  /**
   * Coefficients for erf approximation.
   *
   * Loosely based on http://www.netlib.org/specfun/erf
   */
  static final double erfapp_c[] = { 2.15311535474403846e-8, 5.64188496988670089e-1, 8.88314979438837594e00, 6.61191906371416295e01, 2.98635138197400131e02, 8.81952221241769090e02, 1.71204761263407058e03, 2.05107837782607147e03, 1.23033935479799725E03 };

  /**
   * Coefficients for erf approximation.
   *
   * Loosely based on http://www.netlib.org/specfun/erf
   */
  static final double erfapp_d[] = { 1.00000000000000000e00, 1.57449261107098347e01, 1.17693950891312499e02, 5.37181101862009858e02, 1.62138957456669019e03, 3.29079923573345963e03, 4.36261909014324716e03, 3.43936767414372164e03, 1.23033935480374942e03 };

  /**
   * Coefficients for erf approximation.
   *
   * Loosely based on http://www.netlib.org/specfun/erf
   */
  static final double erfapp_p[] = { 1.63153871373020978e-2, 3.05326634961232344e-1, 3.60344899949804439e-1, 1.25781726111229246e-1, 1.60837851487422766e-2, 6.58749161529837803e-4 };

  /**
   * Coefficients for erf approximation.
   *
   * Loosely based on http://www.netlib.org/specfun/erf
   */
  static final double erfapp_q[] = { 1.00000000000000000e00, 2.56852019228982242e00, 1.87295284992346047e00, 5.27905102951428412e-1, 6.05183413124413191e-2, 2.33520497626869185e-3 };

  /**
   * Fake constructor for static class.
   */
  private MathUtil() {
    // Static methods only - do not instantiate!
  }

  /**
   * Computes the square root of the sum of the squared arguments without under
   * or overflow.
   * 
   * @param a first cathetus
   * @param b second cathetus
   * @return {@code sqrt(a<sup>2</sup> + b<sup>2</sup>)}
   */
  public static double hypotenuse(double a, double b) {
    if(Math.abs(a) > Math.abs(b)) {
      final double r = b / a;
      return Math.abs(a) * Math.sqrt(1 + r * r);
    }
    else if(b != 0) {
      final double r = a / b;
      return Math.abs(b) * Math.sqrt(1 + r * r);
    }
    else {
      return 0.0;
    }
  }

  /**
   * Compute the Mahalanobis distance using the given weight matrix
   * 
   * @param weightMatrix Weight Matrix
   * @param o1_minus_o2 Delta vector
   * @return Mahalanobis distance
   */
  public static double mahalanobisDistance(Matrix weightMatrix, Vector o1_minus_o2) {
    double sqrDist = o1_minus_o2.transposeTimes(weightMatrix).times(o1_minus_o2).get(0);

    if(sqrDist < 0 && Math.abs(sqrDist) < 0.000000001) {
      sqrDist = Math.abs(sqrDist);
    }
    return Math.sqrt(sqrDist);
  }

  /**
   * <p>
   * Provides the Pearson product-moment correlation coefficient for two
   * FeatureVectors.
   * </p>
   * 
   * @param x first FeatureVector
   * @param y second FeatureVector
   * @return the Pearson product-moment correlation coefficient for x and y
   */
  public static double pearsonCorrelationCoefficient(NumberVector<?, ?> x, NumberVector<?, ?> y) {
    final int xdim = x.getDimensionality();
    final int ydim = y.getDimensionality();
    if(xdim != ydim) {
      throw new IllegalArgumentException("Invalid arguments: feature vectors differ in dimensionality.");
    }
    if(xdim <= 0) {
      throw new IllegalArgumentException("Invalid arguments: dimensionality not positive.");
    }
    double sumXX = 0;
    double sumYY = 0;
    double sumXY = 0;
    {
      // Incremental computation
      double meanX = x.doubleValue(1);
      double meanY = y.doubleValue(1);
      for(int i = 2; i <= xdim; i++) {
        // Delta to previous mean
        final double deltaX = x.doubleValue(i) - meanX;
        final double deltaY = y.doubleValue(i) - meanY;
        // Update means
        meanX += deltaX / i;
        meanY += deltaY / i;
        // Delta to new mean
        final double neltaX = x.doubleValue(i) - meanX;
        final double neltaY = y.doubleValue(i) - meanY;
        // Update
        sumXX += deltaX * neltaX;
        sumYY += deltaY * neltaY;
        sumXY += deltaX * neltaY; // should equal deltaY * neltaX!
      }
    }
    final double popSdX = Math.sqrt(sumXX / xdim);
    final double popSdY = Math.sqrt(sumYY / ydim);
    final double covXY = sumXY / xdim;
    if(popSdX == 0 || popSdY == 0) {
      return 0;
    }
    return covXY / (popSdX * popSdY);
  }

  /**
   * <p>
   * Provides the Pearson product-moment correlation coefficient for two
   * FeatureVectors.
   * </p>
   * 
   * @param x first FeatureVector
   * @param y second FeatureVector
   * @return the Pearson product-moment correlation coefficient for x and y
   */
  public static double weightedPearsonCorrelationCoefficient(NumberVector<?, ?> x, NumberVector<?, ?> y, double[] weights) {
    final int xdim = x.getDimensionality();
    final int ydim = y.getDimensionality();
    if(xdim != ydim) {
      throw new IllegalArgumentException("Invalid arguments: feature vectors differ in dimensionality.");
    }
    if(xdim != weights.length) {
      throw new IllegalArgumentException("Dimensionality doesn't agree to weights.");
    }
    // Compute means
    double sumWe;
    double sumXX = 0;
    double sumYY = 0;
    double sumXY = 0;
    {
      // Incremental computation
      double meanX = x.doubleValue(1);
      double meanY = y.doubleValue(1);
      sumWe = weights[0];
      for(int i = 2; i <= xdim; i++) {
        final double weight = weights[i - 1];
        sumWe += weight;
        // Delta to previous mean
        final double deltaX = x.doubleValue(i) - meanX;
        final double deltaY = y.doubleValue(i) - meanY;
        // Update means
        meanX += deltaX * weight / sumWe;
        meanY += deltaY * weight / sumWe;
        // Delta to new mean
        final double neltaX = x.doubleValue(i) - meanX;
        final double neltaY = y.doubleValue(i) - meanY;
        // Update
        sumXX += weight * deltaX * neltaX;
        sumYY += weight * deltaY * neltaY;
        sumXY += weight * deltaX * neltaY; // should equal weight * deltaY *
                                           // neltaX!
      }
    }
    final double popSdX = Math.sqrt(sumXX / sumWe);
    final double popSdY = Math.sqrt(sumYY / sumWe);
    final double covXY = sumXY / sumWe;
    if(popSdX == 0 || popSdY == 0) {
      return 0;
    }
    return covXY / (popSdX * popSdY);
  }

  /**
   * <p>
   * Provides the Pearson product-moment correlation coefficient for two
   * FeatureVectors.
   * </p>
   * 
   * @param x first FeatureVector
   * @param y second FeatureVector
   * @return the Pearson product-moment correlation coefficient for x and y
   */
  public static double pearsonCorrelationCoefficient(double[] x, double[] y) {
    final int xdim = x.length;
    final int ydim = y.length;
    if(xdim != ydim) {
      throw new IllegalArgumentException("Invalid arguments: feature vectors differ in dimensionality.");
    }
    if(xdim <= 0) {
      throw new IllegalArgumentException("Invalid arguments: dimensionality not positive.");
    }
    double sumXX = 0;
    double sumYY = 0;
    double sumXY = 0;
    {
      // Incremental computation
      double meanX = x[0];
      double meanY = y[0];
      for(int i = 1; i < xdim; i++) {
        // Delta to previous mean
        final double deltaX = x[i] - meanX;
        final double deltaY = y[i] - meanY;
        // Update means
        meanX += deltaX / i;
        meanY += deltaY / i;
        // Delta to new mean
        final double neltaX = x[i] - meanX;
        final double neltaY = y[i] - meanY;
        // Update
        sumXX += deltaX * neltaX;
        sumYY += deltaY * neltaY;
        sumXY += deltaX * neltaY; // should equal deltaY * neltaX!
      }
    }
    final double popSdX = Math.sqrt(sumXX / xdim);
    final double popSdY = Math.sqrt(sumYY / ydim);
    final double covXY = sumXY / xdim;
    if(popSdX == 0 || popSdY == 0) {
      return 0;
    }
    return covXY / (popSdX * popSdY);
  }

  /**
   * <p>
   * Provides the Pearson product-moment correlation coefficient for two
   * FeatureVectors.
   * </p>
   * 
   * @param x first FeatureVector
   * @param y second FeatureVector
   * @return the Pearson product-moment correlation coefficient for x and y
   */
  public static double weightedPearsonCorrelationCoefficient(double[] x, double[] y, double[] weights) {
    final int xdim = x.length;
    final int ydim = y.length;
    if(xdim != ydim) {
      throw new IllegalArgumentException("Invalid arguments: feature vectors differ in dimensionality.");
    }
    if(xdim != weights.length) {
      throw new IllegalArgumentException("Dimensionality doesn't agree to weights.");
    }
    // Compute means
    double sumWe;
    double sumXX = 0;
    double sumYY = 0;
    double sumXY = 0;
    {
      // Incremental computation
      double meanX = x[0];
      double meanY = y[0];
      sumWe = weights[0];
      for(int i = 1; i < xdim; i++) {
        final double weight = weights[i];
        sumWe += weight;
        // Delta to previous mean
        final double deltaX = x[i] - meanX;
        final double deltaY = y[i] - meanY;
        // Update means
        meanX += deltaX * weight / sumWe;
        meanY += deltaY * weight / sumWe;
        // Delta to new mean
        final double neltaX = x[i] - meanX;
        final double neltaY = y[i] - meanY;
        // Update
        sumXX += weight * deltaX * neltaX;
        sumYY += weight * deltaY * neltaY;
        sumXY += weight * deltaX * neltaY; // should equal weight * deltaY *
                                           // neltaX!
      }
    }
    final double popSdX = Math.sqrt(sumXX / sumWe);
    final double popSdY = Math.sqrt(sumYY / sumWe);
    final double covXY = sumXY / sumWe;
    if(popSdX == 0 || popSdY == 0) {
      return 0;
    }
    return covXY / (popSdX * popSdY);
  }

  /**
   * Compute the Factorial of n, often written as <code>c!</code> in
   * mathematics.</p>
   * <p>
   * Use this method if for large values of <code>n</code>.
   * </p>
   * 
   * @param n Note: n &gt;= 0. This {@link BigInteger} <code>n</code> will be 0
   *        after this method finishes.
   * @return n * (n-1) * (n-2) * ... * 1
   */
  public static BigInteger factorial(BigInteger n) {
    BigInteger nFac = BigInteger.valueOf(1);
    while(n.compareTo(BigInteger.valueOf(1)) > 0) {
      nFac = nFac.multiply(n);
      n = n.subtract(BigInteger.valueOf(1));
    }
    return nFac;
  }

  /**
   * Compute the Factorial of n, often written as <code>c!</code> in
   * mathematics.
   * 
   * @param n Note: n &gt;= 0
   * @return n * (n-1) * (n-2) * ... * 1
   */
  public static long factorial(int n) {
    long nFac = 1;
    for(long i = n; i > 0; i--) {
      nFac *= i;
    }
    return nFac;
  }

  /**
   * <p>
   * Binomial coefficient, also known as "n choose k".
   * </p>
   * 
   * @param n Total number of samples. n &gt; 0
   * @param k Number of elements to choose. <code>n &gt;= k</code>,
   *        <code>k &gt;= 0</code>
   * @return n! / (k! * (n-k)!)
   */
  public static long binomialCoefficient(long n, long k) {
    final long m = Math.max(k, n - k);
    double temp = 1;
    for(long i = n, j = 1; i > m; i--, j++) {
      temp = temp * i / j;
    }
    return (long) temp;
  }

  /**
   * Compute the Factorial of n, often written as <code>c!</code> in
   * mathematics.
   * 
   * @param n Note: n &gt;= 0
   * @return n * (n-1) * (n-2) * ... * 1
   */
  public static double approximateFactorial(int n) {
    double nFac = 1.0;
    for(int i = n; i > 0; i--) {
      nFac *= i;
    }
    return nFac;
  }

  /**
   * <p>
   * Binomial coefficent, also known as "n choose k")
   * </p>
   * 
   * @param n Total number of samples. n &gt; 0
   * @param k Number of elements to choose. <code>n &gt;= k</code>,
   *        <code>k &gt;= 0</code>
   * @return n! / (k! * (n-k)!)
   */
  public static double approximateBinomialCoefficient(int n, int k) {
    final int m = Math.max(k, n - k);
    long temp = 1;
    for(int i = n, j = 1; i > m; i--, j++) {
      temp = temp * i / j;
    }
    return temp;
  }

  /**
   * Probability density function of the normal distribution.
   * 
   * <pre>
   * 1/(SQRT(2*pi*sigma^2)) * e^(-(x-mu)^2/2sigma^2)
   * </pre>
   * 
   * @param x The value.
   * @param mu The mean.
   * @param sigma The standard deviation.
   * @return PDF of the given normal distribution at x.
   */
  public static double normalPDF(double x, double mu, double sigma) {
    final double x_mu = x - mu;
    final double sigmasq = sigma * sigma;
    return 1 / (Math.sqrt(TWOPI * sigmasq)) * Math.exp(-1 * x_mu * x_mu / 2 / sigmasq);
  }

  /**
   * Cumulative probability density function (CDF) of a normal distribution.
   * 
   * @param x value to evaluate CDF at
   * @param mu Mean value
   * @param sigma Standard deviation.
   * @return The CDF of the normal given distribution at x.
   */
  public static double normalCDF(double x, double mu, double sigma) {
    return (1 + erf(x / Math.sqrt(2))) / 2;
  }

  /**
   * Compute logGamma.
   * 
   * Based loosely on "Numerical Recpies" and the work of Paul Godfrey at
   * http://my.fit.edu/~gabdo/gamma.txt
   * 
   * TODO: find out which approximation really is the best...
   * 
   * @param x Parameter x
   * @return @return log(&#915;(x))
   */
  public static double logGamma(final double x) {
    if(Double.isNaN(x) || (x <= 0.0)) {
      return Double.NaN;
    }
    double g = 607.0 / 128.0;
    double tmp = x + g + .5;
    tmp = (x + 0.5) * Math.log(tmp) - tmp;
    double ser = LANCZOS[0];
    for(int i = LANCZOS.length - 1; i > 0; --i) {
      ser += LANCZOS[i] / (x + i);
    }
    return tmp + Math.log(SQRTTWOPI * ser / x);
  }

  /**
   * Returns the regularized gamma function P(a, x).
   * 
   * Includes the quadrature way of computing.
   * 
   * TODO: find "the" most accurate version of this. We seem to agree with
   * others for the first 10+ digits, but diverge a bit later than that.
   * 
   * @param a Parameter a
   * @param x Parameter x
   * @return Gamma value
   */
  public static double regularizedGammaP(final double a, final double x) {
    // Special cases
    if(Double.isNaN(a) || Double.isNaN(x) || (a <= 0.0) || (x < 0.0)) {
      return Double.NaN;
    }
    if(x == 0.0) {
      return 0.0;
    }
    if(x >= a + 1) {
      // Expected to converge faster
      return 1.0 - regularizedGammaQ(a, x);
    }
    // Loosely following "Numerical Recipes"
    double del = 1.0 / a;
    double sum = del;
    for(int n = 1; n < Integer.MAX_VALUE; n++) {
      // compute next element in the series
      del *= x / (a + n);
      sum = sum + del;
      if(Math.abs(del / sum) < NUM_PRECISION || sum >= Double.POSITIVE_INFINITY) {
        break;
      }
    }
    if(Double.isInfinite(sum)) {
      return 1.0;
    }
    return Math.exp(-x + (a * Math.log(x)) - logGamma(a)) * sum;
  }

  /**
   * Returns the regularized gamma function Q(a, x) = 1 - P(a, x).
   * 
   * Includes the continued fraction way of computing, based loosely on
   * "Numerical Recipes".
   * 
   * TODO: find "the" most accurate version of this. We seem to agree with
   * others for the first 10+ digits, but diverge a bit later than that.
   * 
   * @param a parameter a
   * @param x parameter x
   * @return Result
   */
  public static double regularizedGammaQ(final double a, final double x) {
    if(Double.isNaN(a) || Double.isNaN(x) || (a <= 0.0) || (x < 0.0)) {
      return Double.NaN;
    }
    if(x == 0.0) {
      return 1.0;
    }
    if(x < a + 1.0) {
      // Expected to converge faster
      return 1.0 - regularizedGammaP(a, x);
    }
    // Compute using continued fraction approach.
    final double FPMIN = Double.MIN_VALUE / NUM_PRECISION;
    double b = x + 1 - a;
    double c = 1.0 / FPMIN;
    double d = 1.0 / b;
    double fac = d;
    for(int i = 1; i < Integer.MAX_VALUE; i++) {
      double an = i * (a - i);
      b += 2;
      d = an * d + b;
      if(Math.abs(d) < FPMIN) {
        d = FPMIN;
      }
      c = b + an / c;
      if(Math.abs(c) < FPMIN) {
        c = FPMIN;
      }
      d = 1 / d;
      double del = d * c;
      fac *= del;
      if(Math.abs(del - 1.0) <= NUM_PRECISION) {
        break;
      }
    }
    // From Numerical Recipes:
    return fac * Math.exp(-x + a * Math.log(x) - logGamma(a));
  }

  /**
   * Compute the sum of the i first integers.
   * 
   * @param i maximum summand
   * @return Sum
   */
  public static long sumFirstIntegers(final long i) {
    return ((i - 1L) * i) / 2;
  }

  /**
   * Produce an array of random numbers in [0:1]
   * 
   * @param len Length
   * @return Array
   */
  public static double[] randomDoubleArray(int len) {
    return randomDoubleArray(len, new Random());
  }

  /**
   * Produce an array of random numbers in [0:1]
   * 
   * @param len Length
   * @param r Random generator
   * @return Array
   */
  public static double[] randomDoubleArray(int len, Random r) {
    final double[] ret = new double[len];
    for(int i = 0; i < len; i++) {
      ret[i] = r.nextDouble();
    }
    return ret;
  }

  /**
   * Convert Degree to Radians
   * 
   * @param deg Degree value
   * @return Radian value
   */
  public static double deg2rad(double deg) {
    return deg * Math.PI / 180.0;
  }

  /**
   * Radians to Degree
   * 
   * @param rad Radians value
   * @return Degree value
   */
  public static double rad2deg(double rad) {
    return rad * 180 / Math.PI;
  }

  /**
   * Compute the approximate on-earth-surface distance of two points.
   * 
   * @param lat1 Latitude of first point in degree
   * @param lon1 Longitude of first point in degree
   * @param lat2 Latitude of second point in degree
   * @param lon2 Longitude of second point in degree
   * @return Distance in km (approximately)
   */
  public static double latlngDistance(double lat1, double lon1, double lat2, double lon2) {
    final double EARTH_RADIUS = 6371; // km.
    // Work in radians
    lat1 = MathUtil.deg2rad(lat1);
    lat2 = MathUtil.deg2rad(lat2);
    lon1 = MathUtil.deg2rad(lon1);
    lon2 = MathUtil.deg2rad(lon2);
    // Delta
    final double dlat = lat1 - lat2;
    final double dlon = lon1 - lon2;

    // Spherical Law of Cosines
    // NOTE: there seems to be a signedness issue in this code!
    // double dist = Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) *
    // Math.cos(lat2) * Math.cos(dlon);
    // return EARTH_RADIUS * Math.atan(dist);

    // Alternative: Havestine formula, higher precision at < 1 meters:
    final double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) + Math.sin(dlon / 2) * Math.sin(dlon / 2) * Math.cos(lat1) * Math.cos(lat2);
    final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS * c;
  }

  /**
   * Compute the cosine similarity for two vectors.
   * 
   * @param v1 First vector
   * @param v2 Second vector
   * @return Cosine similarity
   */
  public static double cosineSimilarity(Vector v1, Vector v2) {
    return v1.scalarProduct(v2) / (v1.euclideanLength() * v2.euclideanLength());
  }

  /**
   * Complementary error function for Gaussian distributions = Normal
   * distributions.
   * 
   * Numerical approximation using taylor series. Implementation loosely based
   * on http://www.netlib.org/specfun/erf
   * 
   * @param x parameter value
   * @return erfc(x)
   */
  public static double erfc(double x) {
    if(Double.isNaN(x)) {
      return Double.NaN;
    }
    if(Double.isInfinite(x)) {
      return (x < 0.0) ? 2 : 0;
    }
  
    double result = Double.NaN;
    double absx = Math.abs(x);
    // First approximation interval
    if(absx < 0.46875) {
      double z = x * x;
      result = 1 - x * ((((erfapp_a[0] * z + erfapp_a[1]) * z + erfapp_a[2]) * z + erfapp_a[3]) * z + erfapp_a[4]) / ((((erfapp_b[0] * z + erfapp_b[1]) * z + erfapp_b[2]) * z + erfapp_b[3]) * z + erfapp_b[4]);
    }
    // Second approximation interval
    else if(absx < 4.0) {
      double z = absx;
      result = ((((((((erfapp_c[0] * z + erfapp_c[1]) * z + erfapp_c[2]) * z + erfapp_c[3]) * z + erfapp_c[4]) * z + erfapp_c[5]) * z + erfapp_c[6]) * z + erfapp_c[7]) * z + erfapp_c[8]) / ((((((((erfapp_d[0] * z + erfapp_d[1]) * z + erfapp_d[2]) * z + erfapp_d[3]) * z + erfapp_d[4]) * z + erfapp_d[5]) * z + erfapp_d[6]) * z + erfapp_d[7]) * z + erfapp_d[8]);
      double rounded = Math.round(result * 16.0) / 16.0;
      double del = (absx - rounded) * (absx + rounded);
      result = Math.exp(-rounded * rounded) * Math.exp(-del) * result;
      if(x < 0.0) {
        result = 2.0 - result;
      }
    }
    // Third approximation interval
    else {
      double z = 1.0 / (absx * absx);
      result = z * (((((erfapp_p[0] * z + erfapp_p[1]) * z + erfapp_p[2]) * z + erfapp_p[3]) * z + erfapp_p[4]) * z + erfapp_p[5]) / (((((erfapp_q[0] * z + erfapp_q[1]) * z + erfapp_q[2]) * z + erfapp_q[3]) * z + erfapp_q[4]) * z + erfapp_q[5]);
      result = (ONE_BY_SQRTPI - result) / absx;
      double rounded = Math.round(result * 16.0) / 16.0;
      double del = (absx - rounded) * (absx + rounded);
      result = Math.exp(-rounded * rounded) * Math.exp(-del) * result;
      if(x < 0.0) {
        result = 2.0 - result;
      }
    }
    return result;
  }

  /**
   * Error function for Gaussian distributions = Normal distributions.
   * 
   * Numerical approximation using taylor series. Implementation loosely based
   * on http://www.netlib.org/specfun/erf
   * 
   * @param x parameter value
   * @return erf(x)
   */
  public static double erf(double x) {
    return 1 - erfc(x);
  }
}