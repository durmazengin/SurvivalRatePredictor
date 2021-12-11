function [] = colSurvivalGaussian()
%{
  File   : colSurvivalGaussian
  Author : Engin DURMAZ
  Date   : 07.05.2019
  Description : Procedure calculates the 5 years of survival rates
                by using discriminant function with k-fold of 5
                
                x = {Tumor Size, Nodal, Total Lymph Nodes, Positive Lymph Nodes,
                     Grade, Age, Gender}
                     
                Calculates means and vaiances of first 100 samples
                Then calculates discriminant for each x point to be classified
                
                kfold cross validation applied where K = 5
                
  Dependency: Octave, statistics, (pkg load statistics)
%}

dataAll = dlmread('..\resources\data.txt');

% how many item in all data
rowCount = 780; % not size(dataAll, 1) because after 780, first visit is 2015

% number of cross validation is 5
kFold = 5;

trainingSize = rowCount / kFold;

% group features
for i = 1 : rowCount

  % gorup tumor stage
  if (dataAll(i, 1) == 0)    % If tumor stage 0 and 1 same?
    dataAll(i, 1) = 1;   % set 0 also 1
  endif
    
  % group number of positive nodes
  if (dataAll(i, 3) > 4)             % Positive Node > 4 ?
    dataAll(i, 3) = 5;   % all in same category if bigger than 4
  endif
  
  % group number of total nodes      
  if (dataAll(i,4) > 20)        % Node > 20 ?
    dataAll(i,4) = 1;
  elseif (dataAll(i,4) > 15)    % 20 >= Node > 15 ?
    dataAll(i,4) = 2;
  elseif (dataAll(i,4) > 10)    % 15 >= Node > 10 ?
    dataAll(i,4) = 3;
  elseif (dataAll(i,4) > 5)     % 10 >= Node >  5 ?
    dataAll(i,4) = 4;
  elseif (dataAll(i,4) > 2)     % 5 >= Node > 2 ?
    dataAll(i,4) = 5;
  else                          % 2 >= Node   ?
    dataAll(i,4) = 6;
  endif
  
  % group grades (Poor, Modarate, Well-Differantiated)
  if (dataAll(i, 5) > 1)             % Grade > 1 ? (poor or not)
    dataAll(i, 5) = 2;   % all in same category if bigger than 1
  endif
  
  % group by ages
  if (dataAll(i,6) < 30)       % Age < 30?
    dataAll(i,6) = 1;
  elseif (dataAll(i,6) < 45)   % Age < 45?
    dataAll(i,6) = 2;
  elseif (dataAll(i,6) < 60)   % 45 < Age < 60?
    dataAll(i,6) = 3;
  elseif (dataAll(i,6) < 70)   % 60 < Age < 70?
    dataAll(i,6) = 4;
  elseif (dataAll(i,6) < 75)   % 70 < Age < 75?
    dataAll(i,6) = 5;
  elseif (dataAll(i,6) < 80)   % 75 < Age < 80?
    dataAll(i,6) = 6;
  else                               % 80 < Age ?
    dataAll(i,6) = 7;
  endif
    
endfor

% probabilities for k run (fold)
probSurvival = zeros(kFold, rowCount);

% k-fold cross validation
for k = 1 : kFold 
  startTrain = (k - 1) * trainingSize + 1;
  endTrain = k * trainingSize;

  itemsTraining = dataAll(startTrain:endTrain,:);

  %{
  printf("Range %d - %d\n", startTrain, endTrain);
  disp(itemsTraining);
  %}
  
  % status0 : dead status1 : alive
  status0Indices = itemsTraining(:,8) == 0; % indices of non-livings
  status1Indices = itemsTraining(:,8) == 1; % indices of livings
  status0Features = itemsTraining(status0Indices,:); % features of non-livings
  status1Features = itemsTraining(status1Indices,:); % features of livings

  % means and covariances for all features for status 0 and status 1
  means = zeros(2, 7);
  covars = zeros(2, 7);
  for i = 1 : 7
    means(1, i) = mean(status0Features(:,i));
    covars(1, i) = cov(status0Features(:,i));
    means(2, i) = mean(status1Features(:,i));
    covars(2, i) = cov(status1Features(:,i));
  end
  %{
   printf("\nFold    : %d\n", k);
   printf("Means Death ");
   disp(means(1,:));
   printf("Means Alive ");
   disp(means(2,:));
   
   printf("Covar Death ");
   disp(covars(1,:));
   printf("Covar Alive ");
   disp(covars(2,:));
 %}

  countMatchAll = 0; % # of all matching (correct prediction in general)
  countMatchSrv = 0; % # of survival matching (correct prediction for alive)
  counMatchtDeath = 0; % # of death matching (correct prediction for death)
  countSurv = 0; % # of alive exact values in test data
  countDeath = 0;  # of ex exact values in test data
  
  for i = 1:rowCount % i is for item counter for all data
    
    probSurvival(k, i) = 0; % default set zero (ignore training data)
    
    if (i < startTrain) || (i > endTrain) % apply only test data
      pFeature = zeros(2, 7);
      density0 = 0;
      density1 = 0;
      
      %            T      N    PN     TN    Grade   Age   Gender
      maxVals = [  4      2     5      6     2       7      1  ];
      
      for j = 1:7              % j is for feature counter
          
          x = dataAll(i,j); % arbitrary x from test data index of t
          
          % calculation for non-livings
          pFeature(1, j) = calculateDiscriminant(x, 0.50, means(1, j), covars(1, j));
          
          % calculation for livings
          pFeature(2, j) = calculateDiscriminant(x, 0.50, means(2, j), covars(2, j));
          if( j != 2 )
            density0 += pFeature(1, j) * 100 / maxVals(j);
            density1 += pFeature(2, j) * 100 / maxVals(j);
          endif
      endfor % end features
      
      % set probability for this Fold   
      probSurvival(k, i) = density1 / (density0 + density1);
      
      % decision and comparison with exact value)
      isSurv = 0;
      if (probSurvival(k, i) >= 0.5) % predicted value of that fold
        isSurv = 1;
      endif
    
      if(isSurv == dataAll(i,8)) % exact value of row matching predicted value
        countMatchAll++;
      endif
      
      if(dataAll(i,8) == 1) % if real data is alive
        countSurv++;
        if(isSurv == 1) % and if predicted data also alive
          countMatchSrv++;
        endif
      else                 % if real data : ex
        countDeath++;
        if (isSurv == 0)   % if predicted data also ex
          counMatchtDeath++;
         endif
       endif
      
   endif % end condition for test data
  endfor % end all data visiting
    
   testCount = rowCount - trainingSize;    

   printf("\n");
   printf("Fold %d : Matching | Total | Success Rate\n", k);
   
   rateAll = 100 * countMatchAll / testCount;
   printf("All    :   %3d    |  %3d  |  %02.2f %c\n", countMatchAll, testCount, rateAll, '%');
   
   rateSrv = 100 * countMatchSrv / countSurv;
   printf("Survl  :   %3d    |  %3d  |  %02.2f %c\n", countMatchSrv, countSurv, rateSrv, '%');
   
   rateDeath = 100 * counMatchtDeath / countDeath;
   printf("Death  :   %3d    |  %3d  |  %02.2f %c\n", counMatchtDeath, countDeath, rateDeath, '%');

endfor % end k-fold

% k-fold cross validation results
probKFold = zeros(rowCount);
for i = 1:rowCount % i is for item counter
  for k = 1 : kFold
    probKFold(i) = probKFold(i) + probSurvival(k, i);
  endfor
  probKFold(i) = probKFold(i) / (kFold - 1);
endfor

countMatchAll = 0; % # of all matching (correct prediction in general)
countMatchSrv = 0; % # of survival matching (correct prediction for alive)
counMatchtDeath = 0; % # of death matching (correct prediction for death)
countSurv = 0; % # of alive exact values in test data
countDeath = 0;  # of ex exact values in test data

printf("\nRows Exact Values and Predictions\n");
for i = 1:rowCount % i is for item counter
  isSurv = 0;
  if (probKFold(i) >= 0.5) % predicted value of that fold
    isSurv = 1;
  endif

  if(isSurv == dataAll(i,8)) % exact value of row matching predicted value
    countMatchAll++;
  endif
  
  if(dataAll(i,8) == 1) % if real data is alive
    countSurv++;
    if(isSurv == 1) % and if predicted data also alive
      countMatchSrv++;
    endif
  else                 % if real data : ex
    countDeath++;
    if (isSurv == 0)   % if predicted data also ex
      counMatchtDeath++;
     endif
   endif
  printf("Exact : %d, Test %d ( Surv.Rate : %.0f %c)\n", dataAll(i,8), isSurv, 100 * probKFold(i), '%');
endfor % end test counter


 printf("\nResults of 5-Fold Cross Validation\n");
 printf("        Matching | Total | Success Rate\n");
 rateAll = 100 * countMatchAll / rowCount;
 printf("All   :   %3d    |  %3d  |  %02.2f %c\n", countMatchAll, rowCount, rateAll, '%');

 rateSrv = 100 * countMatchSrv / countSurv;
 printf("Survl :   %3d    |  %3d  |  %02.2f %c\n", countMatchSrv, countSurv, rateSrv, '%');
 
 rateDeath = 100 * counMatchtDeath / countDeath;
 printf("Death :   %3d    |  %3d  |  %02.2f %c\n", counMatchtDeath, countDeath, rateDeath, '%');

 labels = {'Death', 'Surivval', 'All'};
 labels{1} = strcat('Death ( ', num2str(rateDeath),' %)');
 labels{2} = strcat('Survival ( ', num2str(rateSrv),' %)');
 labels{3} = strcat('All ( ', num2str(rateAll),' %)');
 
clf;
yValues = [
  [counMatchtDeath countDeath], 
  [countMatchSrv countSurv], 
  [countMatchAll rowCount]
  ];

h = bar(yValues);
 set (h(1), "facecolor", "g");
 set (h(2), "facecolor", "b");
 set(gca, 'XTickLabel',labels, 'XTick',1:numel(labels))

legendRef = legend('Matching', 'Total');
title ("Success Rates with 5-Fold Cross Validation");

end
