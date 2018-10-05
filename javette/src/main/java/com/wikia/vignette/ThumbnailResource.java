package com.wikia.vignette;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class ThumbnailResource {

  @GET
  @Path("window-crop/height/{height}/width/{width}/x-offset/{x-offset}/y-offset/{y-offset}/window-width/{window-width}/window-height/{window-height}")
  public Response response(
      @PathParam("height") int height,
      @PathParam("width") int width,
      @PathParam("x-offset") int xOffset,
      @PathParam("y-offset") int yOffset,
      @PathParam("window-width") int windowWidth,
      @PathParam("window-height") int windowHeight
  ) throws IOException, InterruptedException {
    Process process = new ProcessBuilder().command(
        "/usr/local/bin/convert",
        "/Users/tomasz/Downloads/10518276764_22b6b7a035_o.jpeg",
        "-extent",
        windowWidth + "x" + windowHeight + "+" + xOffset + "+" + yOffset + "!",
        "-thumbnail",
        windowWidth + "x" + windowHeight,
        "-gravity",
        "center",
        "/Users/tomasz/Downloads/10518276764_22b6b7a035_o_thumbnail.jpeg"
    )
        .start();
    process.waitFor();
    return Response.ok("OK " + process.exitValue(), MediaType.TEXT_PLAIN_TYPE).build();
  }
}
