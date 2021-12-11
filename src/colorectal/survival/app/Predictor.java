package colorectal.survival.app;

/*-----------------------------------------------------------------------------
File         : MainFrame
 Author      : Engin DURMAZ
 Date        : 01.05.2019
 Description : Class which manages graphical user interface
              Get inputs from user like training data path
              Get inputs releated with colorectal cancer
-----------------------------------------------------------------------------*/

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;

public class Predictor  extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	final int INDEX_TUM = 0; // Tumor Stage
	final int INDEX_NOD = 1; // Nodal Stage
	final int INDEX_PLN = 2; // Positive Lymph Nodes
	final int INDEX_TLN = 3; // Total Lymph Nodes
	final int INDEX_GRD = 4; // Grade
	final int INDEX_AGE = 5; // Age
	final int INDEX_SEX = 6; // Gender ( Sex)
	
	private JPanel mainContentPane;
	private JPanel pnlTraining = new JPanel();
	private JPanel pnlFeatures = new JPanel();
	private JTextPane txtDebug = new JTextPane();

	private JTextField txtTrainingImagePath = null;

	JComboBox<String> cbxTumorStage = null;
	JComboBox<String> cbxNodalStage = null;
	private JSpinner spinCancerousNodes= null;	
	private JSpinner spinAllLymphNodes= null;	
	private JTextField txtPathPredictForAll = null; 
	JComboBox<String> cbxTumorGrade = null;
	private JSpinner spinAge= null;	
	private JCheckBox chkMale = null;
	private JCheckBox chkFemale = null;
	
	private int MAIN_FRAME_XPOS = 50;
	private int MAIN_FRAME_YPOS = 50;
	private int MAIN_FRAME_WIDTH = 800;
	private int MAIN_FRAME_HEIGHT = 600;
	

	private int MARGIN_FRAME = 2;
	private int TRAINING_PANEL_HEIGHT = 35;
	private int TRAINING_PANEL_WIDTH = MAIN_FRAME_WIDTH - 2 * MARGIN_FRAME;
	
	private int FEATURES_PANEL_HEIGHT = 220;
	private int FEATURES_PANEL_WIDTH = MAIN_FRAME_WIDTH - 2 * MARGIN_FRAME;
	
	private int DEBUG_PANEL_YPOS = (TRAINING_PANEL_HEIGHT + FEATURES_PANEL_HEIGHT) + 2 * MARGIN_FRAME;
	private int DEBUG_PANEL_HEIGHT = MAIN_FRAME_HEIGHT - DEBUG_PANEL_YPOS - 30;
	private int DEBUG_PANEL_WIDTH = MAIN_FRAME_WIDTH - 2 * MARGIN_FRAME;

	Color TRAINING_BACKGROUND_COLOR = new Color(200, 200, 0);
	Color FEATURES_BACKGROUND_COLOR = new Color(200, 200, 200);
	Color DEBUG_BACKGROUND_COLOR = new Color(255, 255, 255);

	/*
	 * if debug enabled print all processes to textbox on the form
	 * if not enabled, do not update text box; it is disabled when running all dimensions for all
	 * because of faster results 
	 */
	private boolean isDebugEnabled = true;

	private int TRAINING_COUNT = 100;
	
	public static void main(String[] args) 
	{
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() 
			{
				try 
				{					
					Predictor frame = new Predictor();
					frame.setVisible(true);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});

	}
	public void clearDebug() 
	{
		txtDebug.setText("");
	}
	public void writeDebug(String message) 
	{
		if(isDebugEnabled)
		{
			txtDebug.setText(txtDebug.getText() + "\n" + message );
		}
	}

	private void onTrainingClicked() 
	{
		int featureCount = 8;
		// read all lines
		double [][] allData = Utils.readFileToMatrix(txtTrainingImagePath.getText(), 1000, featureCount);
		
		adjustFeatures(allData);
		
		double [][] trainingData = Utils.getPartial(allData, 0, 0, TRAINING_COUNT, featureCount);
		double [][] status0 = Utils.copySpecial(allData, 7, 0);
		double [][] status1 = Utils.copySpecial(allData, 7, 1);
		
		for(int i = 0; i < featureCount; i++)
		{
			double [] columnsI = Utils.getColumn(trainingData, i);
			double meanI = Utils.calculateMean(columnsI);
			double covI = Utils.calculateCov(columnsI);
			
			writeDebug(String.format("Mean %d : %.3f, Covariance %d : %.3f", (i + 1), meanI, (i + 1), covI));
		}
		
		double [][] testData = Utils.readFileToMatrix(txtTrainingImagePath.getText(), TRAINING_COUNT, featureCount);
		
	}

	/*
	 
		  Description : Function calculates the discriminant of arbitrary x point
		                to given mean (muCls) and covariance (covCls) of class
		                dimension : 1
	 */
	private double calculateDiscriminant(double x, double prior, double muCls, double covCls)
	{		
		double discriminant = 0;
		
		// dimension : get size of 1 row of normal distribution matrix
		int dimension = 1;

		// calculate distance : r2 = ((x - µ)'t * (S^-1)*(x - µ))
		double r2 = (x - muCls) * (x - muCls) / covCls;
		/*
		calculate gauss discriminant : 
		    -(distance)/2 - dimension * ln(2*pi)/2 - ln(det(sigma))/2 + ln(prior)
		*/
			
		discriminant = -(r2/2) - (dimension/2)*Math.log(2*Math.PI) - 0.5*Math.log(covCls); 
		discriminant = discriminant + Math.log(prior);
		
		return discriminant;
	}
	
	private void adjustFeatures(double[][] features) 
	{
		// group features
		for(int i = 0; i < features.length; i++)
		{
			// gorup tumor stage
			if (features[i][INDEX_TUM] == 0)    // If tumor stage 0 and 1 same?
			{
				features[i][INDEX_TUM] = 1;   // set 0 also 1
			}

			// group number of positive nodes
			if (features[i][INDEX_PLN] > 4)             // Positive Node > 4 ?
			{
				features[i][INDEX_PLN] = 5;   // all in same category if bigger than 4
			}

			// group number of total nodes      
			if (features[i][INDEX_TLN] > 20)        // Node > 20 ?
			{
				features[i][INDEX_TLN] = 1;

			}
			else if (features[i][INDEX_TLN] > 15)    // 20 >= Node > 15 ?
			{
				features[i][INDEX_TLN] = 2;
			}
			else if (features[i][INDEX_TLN] > 10)    // 15 >= Node > 10 ?
			{
				features[i][INDEX_TLN] = 3;
			}
			else if (features[i][INDEX_TLN] > 5)     // 10 >= Node >  5 ?
			{
				features[i][INDEX_TLN] = 4;
			}
			else if (features[i][INDEX_TLN] > 2)     // 5 >= Node > 2 ?
			{
				features[i][INDEX_TLN] = 5;
			}
			else                          // 2 >= Node   ?
			{
				features[i][INDEX_TLN] = 6;
			}

			// group grades (Poor, Modarate, Well-Differantiated)
			if (features[i][INDEX_GRD] > 1)             // Grade > 1 ? (poor or not)
			{
				features[i][INDEX_GRD] = 2;   // all in same category if bigger than 1
			}

			// group by ages
			if (features[i][INDEX_AGE] < 30)       // Age < 30?
			{
				features[i][INDEX_AGE] = 1;
			}
			else if (features[i][INDEX_AGE] < 45)   // Age < 45?
			{
				features[i][INDEX_AGE] = 2;
			}
			else if (features[i][INDEX_AGE] < 60)   // 45 < Age < 60?
			{
				features[i][INDEX_AGE] = 3;
			}
			else if (features[i][INDEX_AGE] < 70)   // 60 < Age < 70?
			{
				features[i][INDEX_AGE] = 4;
			}
			else if (features[i][INDEX_AGE] < 75)   // 70 < Age < 75?
			{
				features[i][INDEX_AGE] = 5;
			}
			else if (features[i][INDEX_AGE] < 80)   // 75 < Age < 80?
			{
				features[i][INDEX_AGE] = 6;
			}
			else                               // 80 < Age ?
			{
				features[i][INDEX_AGE] = 7;
			}

		}
	}
	private void onPredictClicked() 
	{
		
	}

	private void onCalculateForAllClicked() 
	{
		
	}
	
	public Predictor() 
	{
		setTitle("Predictor for Colorectal Cancer Survival");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(MAIN_FRAME_XPOS, MAIN_FRAME_YPOS, MAIN_FRAME_WIDTH, MAIN_FRAME_HEIGHT);
		mainContentPane = new JPanel();
		mainContentPane.setBackground(SystemColor.inactiveCaption);
		mainContentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		setContentPane(mainContentPane);
		mainContentPane.setLayout(null);
				
		drawTrainingPanel();
		
		drawFeaturesPanel();
		
		drawDebugPanel();
		
	}

	private void drawTrainingPanel() 
	{
		pnlTraining.setBounds(MARGIN_FRAME, MARGIN_FRAME, TRAINING_PANEL_WIDTH, TRAINING_PANEL_HEIGHT);
		mainContentPane.add(pnlTraining);
		pnlTraining.setLayout(null);
		
		JLabel lblTrainingImagePath = new JLabel("Training Features Path");
		lblTrainingImagePath.setBounds(5, 5, 150, 25);
		pnlTraining.add(lblTrainingImagePath);
		
		txtTrainingImagePath = new JTextField(1085);
		txtTrainingImagePath.setBounds(200, 5, 390, 25);
		pnlTraining.add(txtTrainingImagePath);
		
		JButton btnTraining = new JButton("Train");
		btnTraining.setBounds(600, 5, 100, 25);
		pnlTraining.add(btnTraining);
		btnTraining.addActionListener
		(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) 
					{
						onTrainingClicked();
					}
				}
		);

		pnlTraining.setBackground(TRAINING_BACKGROUND_COLOR);
		
		txtTrainingImagePath.setText("resources\\data.txt");
	}
	
	private void drawFeaturesPanel() 
	{
		pnlFeatures.setBounds(MARGIN_FRAME, MARGIN_FRAME + TRAINING_PANEL_HEIGHT, FEATURES_PANEL_WIDTH, FEATURES_PANEL_HEIGHT);
		mainContentPane.add(pnlFeatures);
		pnlFeatures.setLayout(null);

		/** Tumor Stage **/
		JLabel lblTumorStage = new JLabel("Tumor Stage");
		lblTumorStage.setBounds(5, 5, 150, 25);
		pnlFeatures.add(lblTumorStage);
		
		cbxTumorStage = new JComboBox<>();
		cbxTumorStage.addItem("T1");
		cbxTumorStage.addItem("T2");
		cbxTumorStage.addItem("T3");
		cbxTumorStage.addItem("T4a");
		cbxTumorStage.addItem("T4b");
		cbxTumorStage.setBounds(200, 5, 100, 25);
		pnlFeatures.add(cbxTumorStage);
		
		/** Nodal Stage **/
		JLabel lblNodalStage = new JLabel("Nodal Stage");
		lblNodalStage.setBounds(405, 5, 150, 25);
		pnlFeatures.add(lblNodalStage);
		
		cbxNodalStage = new JComboBox<>();
		cbxNodalStage.addItem("N0");
		cbxNodalStage.addItem("N1");
		cbxNodalStage.addItem("N2a");
		cbxNodalStage.addItem("N2b");
		cbxNodalStage.setBounds(600, 5, 100, 25);
		pnlFeatures.add(cbxNodalStage);

		/** Number of positive cancerous lymph nodes **/
		JLabel lblCancerousNodes = new JLabel("Cancerous Nodes");
		lblCancerousNodes.setBounds(5, 35, 150, 25);
		pnlFeatures.add(lblCancerousNodes);
		
		spinCancerousNodes = new JSpinner(new SpinnerNumberModel(0, 0, 16, 1));
		spinCancerousNodes.setBounds(200, 35, 100, 25);
		pnlFeatures.add(spinCancerousNodes);
		
		/** Number of all lymph nodes **/
		JLabel lblAllLymphNodes = new JLabel("All Lymph Nodes");
		lblAllLymphNodes.setBounds(405, 35, 150, 25);
		pnlFeatures.add(lblAllLymphNodes);
		
		spinAllLymphNodes = new JSpinner(new SpinnerNumberModel(0, 0, 45, 1));
		spinAllLymphNodes.setBounds(600, 35, 100, 25);
		pnlFeatures.add(spinAllLymphNodes);
		

		/** Tumor Grade **/
		JLabel lblTumorGrade = new JLabel("Tumor Grade");
		lblTumorGrade.setBounds(5, 65, 150, 25);
		pnlFeatures.add(lblTumorGrade);
		
		cbxTumorGrade = new JComboBox<>();
		cbxTumorGrade.addItem("Poor");
		cbxTumorGrade.addItem("Modarate");
		cbxTumorGrade.addItem("Well-differantiated");
		cbxTumorGrade.setBounds(200, 65, 100, 25);
		pnlFeatures.add(cbxTumorGrade);
		
		/** Age **/
		JLabel lblAge = new JLabel("Age");
		lblAge.setBounds(405, 65, 150, 25);
		pnlFeatures.add(lblAge);
		
		spinAge = new JSpinner(new SpinnerNumberModel(0, 0, 95, 1));
		spinAge.setBounds(600, 65, 100, 25);
		pnlFeatures.add(spinAge);

		/** Gender **/
		JLabel lblGender = new JLabel("Gender");
		lblGender.setBounds(5, 95, 150, 25);
		pnlFeatures.add(lblGender);		
		
		chkMale = new JCheckBox("Male");
		chkMale.setBounds(200, 95, 75, 25);
		pnlFeatures.add(chkMale);
		chkMale.addActionListener
		(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e) 
					{
						onGenderSelected(true);
					}
				}
		);

		chkFemale = new JCheckBox("Female");
		chkFemale.setBounds(280, 95, 75, 25);
		pnlFeatures.add(chkFemale);
		chkFemale.addActionListener
		(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e) 
					{
						onGenderSelected(false);
					}
				}
		);
		chkMale.setSelected(true);
		
		JButton btnPredict = new JButton("Predict");
		btnPredict.setBounds(600, 125, 100, 25);
		pnlFeatures.add(btnPredict);
		btnPredict.addActionListener
		(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) 
					{
						onPredictClicked();
					}
				}
		);

		JLabel lblPathPredictForAll = new JLabel("Path For All");
		lblPathPredictForAll.setBounds(5, 185, 150, 25);
		pnlFeatures.add(lblPathPredictForAll);
		
		txtPathPredictForAll = new JTextField(1085);
		txtPathPredictForAll.setBounds(200, 185, 390, 25);
		pnlFeatures.add(txtPathPredictForAll);
		
		JButton btnPredictForAll = new JButton("Predict All");
		btnPredictForAll.setBounds(600, 185, 100, 25);
		pnlFeatures.add(btnPredictForAll);
		btnPredictForAll.addActionListener
		(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) 
					{
						onCalculateForAllClicked();
					}
				}
		);
		
		pnlFeatures.setBackground(FEATURES_BACKGROUND_COLOR);

	}
	
	private void drawDebugPanel() 
	{

		txtDebug.setBounds(MARGIN_FRAME, DEBUG_PANEL_YPOS, DEBUG_PANEL_WIDTH, DEBUG_PANEL_HEIGHT);
		txtDebug.setFont(new Font("Courier New", Font.PLAIN, 11));
		JScrollPane jspDebug = new JScrollPane(txtDebug);
		jspDebug.setBounds(MARGIN_FRAME, DEBUG_PANEL_YPOS, DEBUG_PANEL_WIDTH-15, DEBUG_PANEL_HEIGHT-15);
		
		mainContentPane.add(jspDebug);

		txtDebug.setBackground(DEBUG_BACKGROUND_COLOR);
	}

	boolean mutexGenderSelection = false;
	private void onGenderSelected(boolean isMale) 
	{
		if(false == mutexGenderSelection)
		{
			mutexGenderSelection = true;
			if(isMale)
			{
				chkFemale.setSelected(!chkMale.isSelected());
			}
			else
			{
				chkMale.setSelected(!chkFemale.isSelected());
			}
			mutexGenderSelection = false;
		}
	}

}
