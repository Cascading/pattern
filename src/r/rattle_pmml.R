## uncomment the following lines to install required libraries
#install.packages("pmml")
#install.packages("randomForest")
#install.packages("rpart.plot")
#install.packages("nnet")
#install.packages("kernlab")

require(graphics)
library(pmml)
library(randomForest)
library(rpart)
library(rpart.plot)
library(nnet)
library(kernlab)

dat_folder <- './data'

## split data into test and train sets

data(iris)
idx <- sample(150, 100)
iris_train <- iris[idx,]
iris_test <- iris[-idx,]

## train a Random Forest model
## example: http://mkseo.pe.kr/stats/?p=220

f <- as.formula("as.factor(Species) ~ .")
fit <- randomForest(f, data=iris, proximity=TRUE, ntree=100)

print(fit$importance)
print(fit)
plot(fit, log="y")
MDSplot(fit, iris$Species)

saveXML(pmml(fit), file=paste(dat_folder, "iris.rf.xml", sep="/"))

## train a Recursive Partition classification tree
## example: http://www.r-bloggers.com/example-9-10-more-regression-trees-and-recursive-partitioning-with-partykit/

f <- as.formula("Species ~ .")
fit <- rpart(f, data=iris_train)

print(fit)
print(table(iris_test$Species, predict(fit, iris_test, type="class")))
prp(fit, extra=1, uniform=F, branch=1, yesno=F, border.col=0, xsep="/")

saveXML(pmml(fit), file=paste(dat_folder, "iris.rpart.xml", sep="/"))

## train a single hidden-layer Neural Network
## example: http://statisticsr.blogspot.com/2008/10/notes-for-nnet.html

samp <- c(sample(1:50,25), sample(51:100,25), sample(101:150,25))

ird <- data.frame(rbind(iris3[,,1], iris3[,,2], iris3[,,3]),
                  species = factor(c(rep("setosa",50), rep("versicolor", 50), rep("virginica", 50))))

f <- as.formula("species ~ .")
fit <- nnet(f, data=ird, subset=samp, size=2, rang=0.1, decay=5e-4, maxit=200)

print(fit)
print(summary(fit))
print(table(ird$species[-samp], predict(fit, ird[-samp,], type = "class")))

saveXML(pmml(fit), file=paste(dat_folder, "iris.nn.xml", sep="/"))

## train a Multinomial Regression model
## example: http://www.jameskeirstead.ca/r/how-to-multinomial-regression-models-in-r/

f <- as.formula("Species ~ .")
fit <- multinom(f, data=iris_train)

print(summary(fit))
print(table(iris_test$Species, predict(fit, iris_test)))

saveXML(pmml(fit, dataset=iris_train), file=paste(dat_folder, "iris.multinom.xml", sep="/"))

## train a Linear Regression model
## example: http://www2.warwick.ac.uk/fac/sci/moac/people/students/peter_cock/r/iris_lm/

f <- as.formula("Sepal.Length ~ .")
fit <- lm(f, data=iris_train)

print(summary(fit))
plot(predict(fit))
plot(fit)

plot(iris$Petal.Length, iris$Petal.Width, pch=21, bg=c("red","green3","blue")[unclass(iris$Species)], main="Edgar Anderson's Iris Data", xlab="Petal length", ylab="Petal width")

saveXML(pmml(fit), file=paste(dat_folder, "iris.lm.xml", sep="/"))

## train a General Linear Regression model (in this case, Logistic Regression)
## example: http://www.stat.cmu.edu/~cshalizi/490/clustering/clustering01.r

cols=c(1,2)
plot(iris[,cols],type="n")
points(iris[iris$Species=="setosa",cols],col=1,pch="*")
points(iris[iris$Species=="versicolor",cols],col=2,pch="*")
points(iris[iris$Species=="virginica",cols],col=3,pch="*")

cols=c(3,4)
plot(iris[,cols],type="n")
points(iris[iris$Species=="setosa",cols],col=1,pch="*")
points(iris[iris$Species=="versicolor",cols],col=2,pch="*")
points(iris[iris$Species=="virginica",cols],col=3,pch="*")

myiris <- cbind(iris,setosa=ifelse(iris$Species=="setosa",1,0))
myiris <- cbind(myiris,versicolor=ifelse(iris$Species=="versicolor",1,0))
myiris <- cbind(myiris,virginica=ifelse(iris$Species=="virginica",1,0))
myiris <- myiris[,-5] # drop the old labels

f <- as.formula("setosa ~ Sepal.Length + Sepal.Width + Petal.Length + Petal.Width")
fit <- glm(f, family=binomial, data=myiris)

print(summary(fit))
plot(fit)

fitted.iris <- round(fitted(fit))
print(table(cbind(fitted.iris, myiris$setosa)))

saveXML(pmml(fit), file=paste(dat_folder, "iris.glm.xml", sep="/"))

## train a Support Vector Machine model
## example: https://support.zementis.com/entries/21176632-what-types-of-svm-models-built-in-r-can-i-export-to-pmml

f <- as.formula("Species ~ .")
fit <- ksvm(f, data=iris_train, type="C-bsvc", kernel=rbfdot(sigma=0.1), C=10, prob.model=TRUE)

print(fit)
print(table(iris_test$Species, predict(fit, iris_test)))

saveXML(pmml(fit, dataset=iris_train), file=paste(dat_folder, "iris.svm.xml", sep="/"))

## train a K-Means clustering model
## example: http://mkseo.pe.kr/stats/?p=15

ds <- iris[,-5]
fit <- kmeans(ds, 3)

plot(iris$Sepal.Length, iris$Sepal.Width, pch = 23, bg = c("blue", "red", "green")[fit$cluster])
points(fit$centers[,c(1,2)], col=1:3, pch=8, cex=2)
table(fit$cluster, iris$Species)

saveXML(pmml(fit), file=paste(dat_folder, "iris.kmeans.xml", sep="/"))

## train a Hierarchical Clustering model
## example: http://mkseo.pe.kr/stats/?p=15

i = as.matrix(iris[,-5])
fit <- hclust(dist(i), method = "average")

initial <- tapply(i, list(rep(cutree(fit, 3), ncol(i)), col(i)), mean)
dimnames(initial) <- list(NULL, dimnames(i)[[2]])

kls = cutree(fit, 3)
print(fit)
plclust(fit)
table(iris$Species, kls)

saveXML(pmml(fit, data=iris, centers=initial), file=paste(dat_folder, "iris.hc.xml", sep="/"))

## TODO:
## pmml.rules
## pmml.rsf
