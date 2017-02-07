//-------------
//- PIE CHART -
//-------------
// Get context with jQuery - using jQuery's .get() method.
var pieChartCanvas = $("#pie-chart").get(0).getContext("2d");
var pieChart = new Chart(pieChartCanvas);
var PieData;

function getStats() {

  var username = $('#username')[0].value
  $.ajax({
      type: 'GET',
      url: '/stat/' + username,
      success: function(data) {
          $('#pie-chart').remove(); // this is my <canvas> element
          $('#pie-chart-container').append('<canvas id="pie-chart" style="height:250px"></canvas>');
          var theCanvas = $("#pie-chart").get(0).getContext("2d");
          var newPieChart = new Chart(theCanvas);
          newPieChart.Doughnut(data, pieOptions);
      }
  });
}
getStats();

var pieOptions = {
//Boolean - Whether we should show a stroke on each segment
segmentShowStroke: true,
//String - The colour of each segment stroke
segmentStrokeColor: "#fff",
//Number - The width of each segment stroke
segmentStrokeWidth: 1,
//Number - The percentage of the chart that we cut out of the middle
percentageInnerCutout: 50, // This is 0 for Pie charts
//Number - Amount of animation steps
animationSteps: 100,
//String - Animation easing effect
animationEasing: "easeOutBounce",
//Boolean - Whether we animate the rotation of the Doughnut
animateRotate: true,
//Boolean - Whether we animate scaling the Doughnut from the centre
animateScale: false,
//Boolean - whether to make the chart responsive to window resizing
responsive: true,
// Boolean - whether to maintain the starting aspect ratio or not when responsive, if set to false, will take up entire container
maintainAspectRatio: false,
//String - A legend template
legendTemplate: "<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<segments.length; i++){%><li><span style=\"background-color:<%=segments[i].fillColor%>\"></span><%if(segments[i].label){%><%=segments[i].label%><%}%></li><%}%></ul>",
//String - A tooltip template
tooltipTemplate: "<%=value %> <%=label%> Tickets"
};
//Create pie or douhnut chart
// You can switch between pie and douhnut using the method below.
pieChart.Doughnut(PieData, pieOptions);
//-----------------
//- END PIE CHART -
//-----------------


$('#perbandingan-status-tiket').change(function() {
  $('#username')[0].value = this.value;
  getStats();
});

// ===========================================================================================================

//-------------
//- PIE CHART -
//-------------
// Get context with jQuery - using jQuery's .get() method.
var pieChartCanvas2 = $("#pie-chart-2").get(0).getContext("2d");
var pieChart2 = new Chart(pieChartCanvas2);
var PieData2;

function getStats2() {

  var username2 = $('#username-2')[0].value
  $.ajax({
      type: 'GET',
      url: '/stat2/' + username2,
      success: function(data2) {
          $('#pie-chart-2').remove(); // this is my <canvas> element
          $('#pie-chart-container-2').append('<canvas id="pie-chart-2" style="height:230px"></canvas>');
          var theCanvas2 = $("#pie-chart-2").get(0).getContext("2d");
          var newPieChart2 = new Chart(theCanvas2);
          newPieChart2.Doughnut(data2, pieOptions2);
      }
  });
}
getStats2();

var pieOptions2 = {
//Boolean - Whether we should show a stroke on each segment
segmentShowStroke: true,
//String - The colour of each segment stroke
segmentStrokeColor: "#fff",
//Number - The width of each segment stroke
segmentStrokeWidth: 1,
//Number - The percentage of the chart that we cut out of the middle
percentageInnerCutout: 50, // This is 0 for Pie charts
//Number - Amount of animation steps
animationSteps: 100,
//String - Animation easing effect
animationEasing: "easeOutBounce",
//Boolean - Whether we animate the rotation of the Doughnut
animateRotate: true,
//Boolean - Whether we animate scaling the Doughnut from the centre
animateScale: false,
//Boolean - whether to make the chart responsive to window resizing
responsive: true,
// Boolean - whether to maintain the starting aspect ratio or not when responsive, if set to false, will take up entire container
maintainAspectRatio: false,
//String - A legend template
legendTemplate: "<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<segments.length; i++){%><li><span style=\"background-color:<%=segments[i].fillColor%>\"></span><%if(segments[i].label){%><%=segments[i].label%><%}%></li><%}%></ul>",
//String - A tooltip template
tooltipTemplate: "<%=value %> <%=label%> Priority Tickets"
};
//Create pie or douhnut chart
// You can switch between pie and douhnut using the method below.
pieChart2.Doughnut(PieData2, pieOptions2);
//-----------------
//- END PIE CHART -
//-----------------


$('#perbandingan-prioritas-tiket').change(function() {
  $('#username-2')[0].value = this.value;
  getStats2();
});