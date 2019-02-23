package frc.robot;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

public class Encoder
{

    TalonSRX moTal;

    // drewnote: encoder testing
    final int timeoutMs = 30;
    final boolean discontinuity = true;
    final int bookend0 = 910; //80 deg
    final int bookend1 = 1137; //100 deg
    

    public Encoder(TalonSRX motor, int id)
    {
        moTal = motor;

        initQuad();

        moTal.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, timeoutMs);
        
    }

    public void zero()
    {
        moTal.setSelectedSensorPosition(0);
    }

    
    public void initQuad()
    {
		/* get the absolute pulse width position */
		int pulseWidth = moTal.getSensorCollection().getPulseWidthPosition();

		/**
		 * If there is a discontinuity in our measured range, subtract one half
		 * rotation to remove it
		 */
		if (discontinuity) {

			/* Calculate the center */
			int newCenter;
			newCenter = (bookend0 + bookend1) / 2;
			newCenter &= 0xFFF;

			/**
			 * Apply the offset so the discontinuity is in the unused portion of
			 * the sensor
			 */
			pulseWidth -= newCenter;
		}

		/**
		 * Mask out the bottom 12 bits to normalize to [0,4095],
		 * or in other words, to stay within [0,360) degrees 
		 */
		pulseWidth = pulseWidth & 0xFFF;

		/* Update Quadrature position */
        //moTal.getSensorCollection().setQuadraturePosition(pulseWidth, timeoutMs);
        moTal.setSelectedSensorPosition(0, 0, timeoutMs);
        //moTal.getSensorCollection().set
    }

    /**
     * @param units CTRE mag encoder sensor units 
     * @return degrees rounded to tenths.
     */
    private double deg(int units) {
        double deg = units * 360.0 / 4096.0;
 
        /* truncate to 0.1 res */
        deg *= 10;
        deg = (int) deg;
        deg /= 10;

        return deg;
    }
    
    public double angle()//int tolerance)
    {
        // disabled to do tolerance elsewhere
        //double encoder = ((int) position() / tolerance) * tolerance;
        double angle = 0;
        
        angle = position() / 56.9;
        
        
        return angle;
    }

    public double position()
    {
        return moTal.getSelectedSensorPosition();
    }

    public double velocity()
    {
        return moTal.getSelectedSensorVelocity();
    }

}
