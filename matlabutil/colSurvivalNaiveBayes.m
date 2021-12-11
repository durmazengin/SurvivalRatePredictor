function [] = colSurvivalNaiveBayes()
%{
  File   : colSurvivalNaiveBayes
  Author : Engin DURMAZ
  Date   : 10.05.2019
  Description : Procedure calculates the 5 years of survival rates
                by naive bayes: P(w|x_i) = P(x_i|w) * P(w)
                                
                x = {Tumor Size, Nodal, Total Lymph Nodes, Positive Lymph Nodes,
                     Grade, Age, Gender}
                
                Then calculates posterior for each x point to be classified
                
                Note: Data consist of 7 features but Nodal is not used
                In other words 6 features are used
                
  Dependency: Octave, statistics, (pkg load statistics)
%}

dataAll = dlmread('..\resources\data.txt');

% how many item in all data
rowCount = size(dataAll, 1);
% featuresC : sample featues of data to be classified
featuresC = dataAll(1: rowCount,:);

probSurvival = zeros(1, rowCount);
for i = 1:rowCount % i is for item counter

  pSurvT = 0.5;  
  if (featuresC(i,1) == 4)       % TumorSize == 4?
    pSurvT = 0.45;
  elseif (featuresC(i,1) == 3)   % TumorSize == 3?
    pSurvT = 0.65;
  elseif (featuresC(i,1) == 2)   % TumorSize == 2?
    pSurvT = 0.85;
  else                           % TumorSize = 1
    pSurvT = 0.95;
  endif
      
  pTotalNodes = 1; % pTotalNodes effect of total nodes
  if (featuresC(i,4) > 20)        % Node > 20 ?
    pTotalNodes = 1;
  elseif (featuresC(i,4) > 15)    % 20 >= Node > 15 ?
    pTotalNodes = 0.90;
  elseif (featuresC(i,4) > 10)    % 15 >= Node > 10 ?
    pTotalNodes = 0.80;
  elseif (featuresC(i,4) > 5)     % 10 >= Node >  5 ?
    pTotalNodes = 0.65;
  elseif (featuresC(i,4) > 2)     % 5 >= Node > 2 ?
    pTotalNodes = 0.55;
  else                            % 2 >= Node > 0 ?
    pTotalNodes = 0.10;
  endif

  pPositiveNodes = 1;
  if (featuresC(i,3) > 4)             % Positive Node > 4 ?
    pSurvN = 0.01 * pTotalNodes;
  elseif (featuresC(i,3) == 4)        % Positive Node = 4 ?
    pSurvN = 0.10 * pTotalNodes;
  elseif (featuresC(i,3) == 3)        % Positive Node = 3 ?
    pSurvN = 0.35 * pTotalNodes;
  elseif (featuresC(i,3) == 2)        % Positive Node = 2 ?
    pSurvN = 0.55 * pTotalNodes;
  elseif (featuresC(i,3) == 1)        % Positive Node = 1 ?
    pSurvN = 0.80 * pTotalNodes;
  else                                % Positive Node = 0
    pSurvN = pTotalNodes;
  endif
  
  pSurvG = 0.70;  
  if (featuresC(i,5) == 1)       % Grade == Poor?
    pSurvG = 0.30;
  endif
  
  pSurvA = 0.40;  
  if (featuresC(i,6) < 30)       % Age < 30?
    pSurvA = 1;
  elseif (featuresC(i,6) < 45)   % Age < 45?
    pSurvA = 0.95;
  elseif (featuresC(i,6) < 60)   % 45 < Age < 60?
    pSurvA = 0.75;
  elseif (featuresC(i,6) < 70)   % 60 < Age < 70?
    pSurvA = 0.45;
  elseif (featuresC(i,6) < 75)   % 70 < Age < 75?
    pSurvA = 0.25;
  elseif (featuresC(i,6) < 80)   % 75 < Age < 80?
    pSurvA = 0.10;
  else                               % 80 < Age ?
    pSurvA = 0.05;
  endif

  pSurvS = 0.40;  
  if (featuresC(i,7) == 1)        % Gender == Female?
    pSurvS = 0.50;
  endif
  
  prob = pSurvT * 0.10 + pSurvN * 0.35 + pSurvA * 0.45 + pSurvG * 0.06 + pSurvS * 0.04;
  probSurvival(1, i) = prob;
endfor

countMatching = 0;
countMatchSurv = 0;
countMatchDeath = 0;
countSurv = 0;
countDeath = 0;

printf("\nRows Exact Values and Predictions\n");

for i = 1:rowCount % i is for item counter
  isSurv = 0;
  if (probSurvival(1, i) >= 0.5)
    isSurv = 1;
  endif

  if(isSurv == featuresC(i,8))
    countMatching++;
  endif
  if(featuresC(i,8) == 1)
      countSurv++;
    if(isSurv == 1)
      countMatchSurv++;
    endif
   else
    countDeath++;
    if (isSurv == 0)
      countMatchDeath++;
    endif
   endif
  printf("Exact : %d, Test %d ( Surv.Rate : %.0f %c)\n", featuresC(i,8), isSurv, 100*probSurvival(1, i), '%');
endfor
 printf("\n");
 printf("        Matching | Total | Success Rate\n");

rateAll = countMatching * 100 / rowCount;
printf("All   :   %3d    |  %3d  |  %02.2f %c\n", countMatching, rowCount, rateAll, '%');

rateSurv = countMatchSurv * 100 / countSurv;
printf("Survl :   %3d    |  %3d  |  %02.2f %c\n", countMatchSurv, countSurv, rateSurv, '%');

rateDeath = countMatchDeath * 100 / countDeath;
printf("Death :   %3d    |  %3d  |  %02.2f %c\n", countMatchDeath, countDeath, rateDeath, '%');

labels = {'Death', 'Surivval', 'All'};
labels{1} = strcat('Death ( ', num2str(rateDeath),' %)');
labels{2} = strcat('Survival ( ', num2str(rateSurv),' %)');
labels{3} = strcat('All ( ', num2str(rateAll),' %)');

clf;
yValues = [
  [countMatchDeath countDeath], 
  [countMatchSurv countSurv], 
  [countMatching rowCount]
  ];

h = bar(yValues);
 set (h(1), "facecolor", "g");
 set (h(2), "facecolor", "b");
 set(gca, 'XTickLabel',labels, 'XTick',1:numel(labels))

legendRef = legend('Matching', 'Total');
title ("Success Rates with Naive Bayes");

end
