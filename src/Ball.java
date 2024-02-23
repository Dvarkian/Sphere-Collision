import java.awt.Graphics; // Used for drawing components.
import java.awt.Color;
import java.awt.Point;
import java.awt.Font;
import java.awt.geom.Point2D;


public class Ball {

    private double x, y;

    private double vx, vy;

    private double ax, ay;

    private int radius;

    private int mass;

    private Color color;

    public Ball(double x, double y, double vx, double vy, double ax, double ay, int radius, Color color) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.ax = ax;
        this.ay = ay;
        this.radius = radius;
        this.mass = radius;
        this.color = color;
    }

    public void move(int topBound, int bottomBound, int leftBound, int rightBound, double dt) {


        // Update position of ball based on current position.

        int wallHits = 0;


        if (x - radius <= leftBound) {
            vx = Math.abs(vx);
            wallHits += 1;
        } else if (x + radius >= rightBound) {
            vx = -Math.abs(vx);
            wallHits += 1;
        }

        if (y - radius <= bottomBound) {
            vy = Math.abs(vy) * 0.96;
            wallHits += 1;
        } else if (y + radius  >= topBound) {
            vy = -Math.abs(vy);
            wallHits += 1;
        }



        x += vx * dt + 0.5 * ax * dt * dt;;
        y += vy * dt + 0.5 * ay * dt * dt;

        vx = vx + ax * dt;
        vy = vy + ay * dt;

    }


    Point2D.Double closestPointOnLine(double lx1, double ly1, double lx2, double ly2, double x0, double y0){
        // Start with a given point (x0, y0) and a given line segment, described by endpoints (lx1, ly1) and (lx2, ly2).

        // Take the endpoints of the line segment and turn it into an equation of the form Ax + By = C.
        double A = ly2 - ly1;
        double B = lx1 - lx2;
        double C1 = A*lx1 + B*ly1;

        // The equation of the line perpendicular to the initial line segment is given by  -Bx + Ay = C,
        // but this time (x, y) is the given point so that the new equation crosses through the given point.
        double C2 = -B*x0 + A*y0;

        // Find the determinant of the two equations algebraically:
        double det = A*A + B*B;


        // Use Cramer's Rule to solve for the point of intersection of the original line and the perpendicular line,
        // and that gives us the closest point on the given line to given point.
        double cx = 0;
        double cy = 0;

        if(det != 0){
            cx = (A*C1 - B*C2) / det;
            cy = (A*C2 + B*C1) / det;
        } else {
            // If the determinant = 0 , then the point is on the line,
            // and thus the closest point on the line to the point is the point itself!
            cx = x0;
            cy = y0;
        }
        return new Point2D.Double(cx, cy);
    }


    // Inside your Ball class
    public void collide(Ball other, int windowHeight, int simulationPaneTopOffset, double dt) {

        // We know that both balls collide, by this point. Do not need to check for collision again.

        // Calculate initial total energy
        double initEnergy = getTotalEnergy(windowHeight, ay) + other.getTotalEnergy(windowHeight, other.ay);

        double sx = other.x - x;
        double sy = other.y - y;

        double dist = Math.sqrt(sx * sx + sy * sy); // Calculate distance between the balls.
        // Could make this more efficient by only calculating once.
        // Also calculated when checking for collision.
        int totalRadius = radius + other.radius;
        double totalMass = mass + other.mass;

        double overlap = totalRadius - dist;

        x -= overlap * (sx / dist) * (other.mass / totalMass) ;
        y -= overlap * (sy / dist) * (other.mass / totalMass) ;
        other.x += overlap * (sx / dist) * (mass / totalMass) ;
        other.y += overlap * (sy / dist) * (mass / totalMass) ;


        double nx = sx / dist;
        double ny = sy / dist;



        // Gets closest point on line along ball's velocity, to other ball.
        Point2D.Double closestPoint = closestPointOnLine(x, y, x + vx, y + vy, other.x, other.y);

        // Calculate square of distance from closest point on line to other ball.
        double closestdistsq = Math.pow(other.x - closestPoint.x, 2) + Math.pow(other.y - closestPoint.y, 2);



        // Dynamic colision

        double backdist = Math.sqrt(Math.abs(totalRadius * totalRadius - closestdistsq));
        double movementvectorlength = Math.sqrt(vx * vx + vy * vy);

        double c_x = closestPoint.x - backdist * (vx / movementvectorlength);
        double c_y = closestPoint.y - backdist * (vy / movementvectorlength);

        double collisionDist = Math.sqrt(Math.pow(other.x - c_x, 2) + Math.pow(other.y - c_y, 2)) + 1E-16;
        double n_x = (other.x - c_x) / collisionDist;
        double n_y = (other.y - c_y) / collisionDist;

        double p = 2 * (vx * nx + vy * n_y - other.vx * nx - other.vy * n_y) / (mass + other.mass);

        double vx1 = vx - p * other.mass * n_x;
        double vy1 = vy - p * other.mass * n_y;
        double vx2 = other.vx + p * mass * n_x;
        double vy2 = other.vy + p * mass * n_y;





        vx = vx1;
        vy = vy1;

        other.vx = vx2;
        other.vy = vy2;


        double finalEnergy = getTotalEnergy(windowHeight, ay) + other.getTotalEnergy(windowHeight, other.ay);
        double finalKE = getKineticEnergy() + other.getKineticEnergy();

        if (Math.abs(initEnergy - finalEnergy) > 1E-6) {

            double energyDiscrep = finalEnergy - initEnergy;

            double sign = - Math.signum(energyDiscrep);

            double v1sq = vx * vx + vy * vy;
            double v2sq = other.vx * other.vx + other.vy * other.vy;

            double v1 = Math.sqrt(v1sq);
            double v2 = Math.sqrt(v2sq);

            double deltav1sq = 2 * Math.abs(energyDiscrep) / (mass + other.mass * v2sq / v1sq);
            double deltav2sq = 2 * Math.abs(energyDiscrep) / (other.mass + mass * v1sq / v2sq);

            double correctedEnergy1 = 0.5 * mass * deltav1sq ;
            double correctedEnergy2 = 0.5 * other.mass * deltav2sq;

            double targetKE1 = getKineticEnergy() + sign * correctedEnergy1;
            double targetKE2 = other.getKineticEnergy() + sign * correctedEnergy2;

            double targetV1 = v1 * Math.sqrt(targetKE1 / getKineticEnergy());
            double targetV2 = v2 * Math.sqrt(targetKE2 / other.getKineticEnergy());

            vx = targetV1 * vx / v1;
            vy = targetV1 * vy / v1;
            other.vx = targetV2 * other.vx / v2;
            other.vy = targetV2 * other.vy / v2;

            System.out.println("Initial Energy Discrep.: " + String.format("%.0f", (finalEnergy - initEnergy)) +
                    " sign: " + sign +
                    " Corr " + String.format("%.0f , %.0f",  correctedEnergy1, correctedEnergy2) +
                    String.format("  Post - Pre: %.0f", 0.5*mass*(vx*vx+vy*vy) - 0.5*mass*v1*v1)
                    //String.format("DeltaV1 %.0f , target: %.0f", Math.sqrt(vx*vx+vy*vy)-v1, deltav1)
                    //" Correcting: " + String.format("%.0f", correctedEnergy) +
                    //" Actual corrected energy: " + String.format("%.0f", postKE - preKE) +
                    );

        }




    }

    public boolean collidesWith(Ball other, double dt) {
        double sx = other.x - x;
        double sy = other.y - y;

        //double sx = other.x + other.vx * dt - x + vx * dt;
        //double sy = other.y + other.vy * dt - y + vy * dt;

        double distance = Math.sqrt(sx * sx + sy * sy);

        return distance < radius + other.radius - 0.5; // Returns true if 2 balls undergo a collision.
    }

    public double getKineticEnergy() {
        double ke = 0.5 * mass * (vx * vx + vy * vy);
        return ke ;
    }

    public double getGravitationalPotentialEnergy(int windowHeight, double g) {
        double gpe = mass * Math.abs(g) * (windowHeight - y - radius);
        return gpe ;
    }

    public double getTotalEnergy(int windowHeight, double g) {
        return getKineticEnergy() + getGravitationalPotentialEnergy(windowHeight, g);
    }

    public void render(Graphics g) {

        g.setColor(color);
        g.fillOval((int) (x-radius), (int) (y-radius), radius * 2, radius * 2);  // You can adjust the size as needed

        // Set the color for the outline (you can choose your own color)
        g.setColor(Color.BLACK);
        g.drawOval((int) (x - radius), (int) (y - radius), radius * 2, radius * 2);


    }


}


