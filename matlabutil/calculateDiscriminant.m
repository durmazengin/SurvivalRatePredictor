function  [discriminant] = calculateDiscriminant(x, prior, muCls, covCls)
%{
  File   : calculateDiscriminant
  Author : Engin DURMAZ
  Date   : 07.05.2019 
  Description : Function calculates the discriminant of arbitrary x point
                to given mean (muCls) and covariance (covCls) of class
                dimension : 1
  Dependency: Octave, statistics, (pkg load statistics)
%}

% dimension : get size of 1 row of normal distribution matrix
dimension = 1;

% calculate distance : r2 = ((x - µ)'t * (S^-1)*(x - µ))
r2 = ((x - muCls)') * inv(covCls) * (x - muCls);
% calculate gauss discriminant : 
%     -(distance)/2 - dimension * ln(2*pi)/2 - ln(det(sigma))/2 + ln(prior)
discriminant = -(r2/2) - (dimension/2)*log(2*pi) - 0.5*log(det(covCls)); 
discriminant = discriminant + log(prior);

end
